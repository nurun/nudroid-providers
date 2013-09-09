package com.nudroid.persistence.annotation.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nurun.persistence.annotation.Authority;
import com.nurun.persistence.annotation.Query;

/**
 * Annotation processor creating the bindings necessary for Android content provider delegates. TODO Finish
 * documentation
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@SupportedAnnotationTypes({ "com.nurun.persistence.annotation.Authority", "com.nurun.persistence.annotation.Delete",
        "com.nurun.persistence.annotation.Insert", "com.nurun.persistence.annotation.Projection",
        "com.nurun.persistence.annotation.Query", "com.nurun.persistence.annotation.QueryParam",
        "com.nurun.persistence.annotation.Selection", "com.nurun.persistence.annotation.SelectionArgs",
        "com.nurun.persistence.annotation.SortOrder", "com.nurun.persistence.annotation.Update" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PersistenceAnnotationProcessor extends AbstractProcessor {

    static final String GENERATED_CODE_BASE_PACKAGE = "com.nudroid.persistence";
    static final String CONTENT_URI_REGISTRY_CLASS_NAME = "ContentUriRegistry";

    private static final String CONTENT_URI_REGISTRY_TEMPLATE_LOCATION = "com/nudroid/persistence/annotation/processor/ContentUriRegistryTemplate.vm";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Elements elementUtils;
    private Filer filer;
    private boolean initialized = false;
    private int iterationRun = 0;

    private ProcessorContinuation continuation;
    private Metadata metadata;

    @Override
    public synchronized void init(ProcessingEnvironment env) {

        super.init(env);

        logger.info("Initializing nudroid persistence annotation processor.");

        elementUtils = env.getElementUtils();
        filer = env.getFiler();

        continuation = new ProcessorContinuation(filer);
        metadata = new Metadata();

        try {
            continuation.loadContinuation();
            initialized = true;
            logger.info("Initialization complete.");
        } catch (Exception e) {
            logger.error("Unable to load continuation index file. Aborting annotation processor.", e);
        }
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
     *      javax.annotation.processing.RoundEnvironment)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        iterationRun++;

        if (!initialized) {

            logger.error("Annotation processor not initialized. Aborting.");

            return false;
        }

        if (isFirstRun()) {

            Set<Element> elementsToProcess = getElementsToProcess(roundEnv);

            logger.info("[{}] Processing elements: {}", iterationRun, elementsToProcess);

            for (Element rootClass : elementsToProcess) {

                processAuthorityOnClass(rootClass);
                processQueriesOnClass(rootClass);
            }

            generateCompanionSourceCode(metadata);
            generateContinuationFile();
        }

        return true;
    }

    private boolean isFirstRun() {

        return iterationRun == 1;
    }

    private Set<Element> getElementsToProcess(RoundEnvironment roundEnv) {
        Set<Element> classesToProcess = new HashSet<Element>();
        classesToProcess.addAll(roundEnv.getRootElements());
        Set<? extends Element> previousDelegateElements = continuation.loadDelegateElements(elementUtils);

        previousDelegateElements.removeAll(classesToProcess);

        if (!previousDelegateElements.isEmpty()) {

            logger.info("[{}] Adding continuation elements from previous builds: {}", iterationRun,
                    previousDelegateElements);
            classesToProcess.addAll(previousDelegateElements);
        }

        return classesToProcess;
    }

    private void processAuthorityOnClass(Element rootClass) {

        Authority authority = rootClass.getAnnotation(Authority.class);

        if (authority != null) {

            continuation.addContentProviderDelegate(rootClass);

            if (isAbstract(rootClass)) {

                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "@Authority annotations are ignored on abstract elements since they can't be instantiated.",
                        rootClass);
            } else {

                String authorityString = metadata.parseAuthorityFromClass(rootClass);

                TypeElement annotatedType = metadata.getClassForAuthority(authorityString);

                if (annotatedType == null || annotatedType.equals(rootClass)) {

                    metadata.setClassForAuthority(authorityString, (TypeElement) rootClass);
                } else {

                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format("Class %s already defines an authority named '%s'.", annotatedType,
                                    authorityString), rootClass);
                }
            }
        }
    }

    private void processQueriesOnClass(Element rootClass) {

        List<? extends Element> methodElementsList = elementUtils.getAllMembers(elementUtils.getTypeElement(rootClass
                .toString()));

        for (Element targetElement : methodElementsList) {

            processQueryOnMethod(rootClass, targetElement);
        }
    }

    private void processQueryOnMethod(Element rootClass, Element method) {
        Query query = method.getAnnotation(Query.class);

        if (query == null) return;

        continuation.addContentProviderDelegate(rootClass);

        if (isInterface(rootClass)) {

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "Java does not inherit annotations from methods. "
                            + "The @Query annotation in this method won't be inherited by implementing classes.",
                    method);

            return;
        }

        if (isAbstract(rootClass)) {

            return;
        }

        if (!validateQueryElement(rootClass, method)) return;

        logger.info("[{}] Processing Query annotation on {}", iterationRun, rootClass + "." + method);

        try {

            metadata.addUri(metadata.parseAuthorityFromClass(rootClass), query.value());

        } catch (DuplicateUriParameterException e) {

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format("Duplicated parameter. Parameter '%s', appearing at position '%d' "
                            + "is already present at position '%d'.", e.getParamName(), e.getDuplicatePosition(),
                            e.getExistingPosition()), method);
        }

        metadata.addTargetMethod((TypeElement) rootClass, (ExecutableElement) method);
    }

    private boolean validateQueryElement(Element rootClass, Element method) {

        boolean isValid = true;
        Element enclosingElement = method.getEnclosingElement();
        Element parentEnclosingElement = enclosingElement.getEnclosingElement();

        if (!validateClassIsAnnotatedWithAuthority(method, rootClass)) {
            isValid = false;
        }

        if (!validateClassIsTopLevelOrStatic(method, enclosingElement, parentEnclosingElement)) {
            isValid = false;
        }

        if (isClass(enclosingElement) && !validateClassHasDefaultConstructor(enclosingElement)) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateClassIsAnnotatedWithAuthority(Element method, Element methodEnclosingClass) {

        boolean isValid = true;

        if (methodEnclosingClass.getAnnotation(Authority.class) == null) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Class needs to be annotated with @Authority annotation.", method);

            isValid = false;
        }

        return isValid;
    }

    private boolean validateClassIsTopLevelOrStatic(Element method, Element methodEnclosingClass,
            Element parentparentClassElement) {

        boolean isValid = true;

        if (!isClassOrInterface(methodEnclosingClass)) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Query annotations can only appear on class and interface methods.", method);

            isValid = false;
        }

        Set<Modifier> modifiers = methodEnclosingClass.getModifiers();

        if (isClassOrInterface(parentparentClassElement) && !modifiers.contains(Modifier.STATIC)) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Query annotations can only appear on top level or static classes.", method);

            isValid = false;
        }

        return isValid;
    }

    private boolean validateClassHasDefaultConstructor(Element rootClass) {

        List<? extends Element> enclosedElements = rootClass.getEnclosedElements();

        for (Element child : enclosedElements) {

            if (child.getKind().equals(ElementKind.CONSTRUCTOR)) {

                if (((ExecutableElement) child).getParameters().size() == 0) {
                    return true;
                }
            }
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Classes annotated with content provider annotations must provide a default constructor.", rootClass);

        return false;
    }

    private void generateCompanionSourceCode(Metadata metadata) {

        Properties p = generateVelocityConfigurationProperties();
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put("contentProviders", metadata.getTargetClasses());
        context.put("authoritiesAndUris", metadata.getAuthoritiesAndUris());

        Template template = null;

        try {
            template = Velocity.getTemplate(CONTENT_URI_REGISTRY_TEMPLATE_LOCATION);
            JavaFileObject jfoContentUriRegistry = filer.createSourceFile(String.format("%s.%s",
                    GENERATED_CODE_BASE_PACKAGE, CONTENT_URI_REGISTRY_CLASS_NAME));
            Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();

            template.merge(context, writerContentUriRegistry);
            writerContentUriRegistry.close();
        } catch (ResourceNotFoundException e) {
            debug("Error Loading script..." + e);
            e.printStackTrace();
        } catch (ParseErrorException e) {
            debug("Error Loading script..." + e);
            e.printStackTrace();
        } catch (MethodInvocationException e) {
            debug("Error Loading script..." + e);
            e.printStackTrace();
        } catch (Exception e) {
            debug("Error Loading script..." + e.getCause());
            e.printStackTrace();
        }

    }

    private Properties generateVelocityConfigurationProperties() {
        Properties p = new Properties();
        p.put("resource.loader", "classpath");
        p.put("classpath.resource.loader.description", "Velocity Classpath Resource Loader");
        p.put("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return p;
    }

    private void generateContinuationFile() {

        Set<String> indexedTypes = continuation.getContentProviderDelegateNames();

        try {
            FileObject indexFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, GENERATED_CODE_BASE_PACKAGE,
                    ProcessorContinuation.CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME);
            Writer writer = indexFile.openWriter();
            PrintWriter printWriter = new PrintWriter(writer);

            for (String indexedTypeName : indexedTypes) {
                printWriter.println(indexedTypeName);
            }

            printWriter.close();
            writer.close();
        } catch (IOException e) {
            debug("Error " + e);
        }
    }

    // private void generateCompanionSourceCode(Metadata metadata) throws IOException {
    //
    // JavaFileObject jfoContentUriRegistry = filer
    // .createSourceFile("com.nurun.persistence.temp.vision.ContentUriRegistry");
    // Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();
    // JavaWriter javaWriterContentUriRegistry = new JavaWriter(writerContentUriRegistry);
    //
    // writeContentUriRegistryPackage(javaWriterContentUriRegistry);
    // writeContentUriRegistryImports(metadata, javaWriterContentUriRegistry);
    // writeContentUriRegistryBeginClass(javaWriterContentUriRegistry);
    // writeContentUriRegistryUriMatcherInitializer(javaWriterContentUriRegistry);
    // writeContentUriRegistryGetRoutForMethodSignature(javaWriterContentUriRegistry);
    //
    // for (TypeElement targetElement : metadata.getTargetClasses()) {
    //
    // String routerClassName = "com.nurun.persistence.temp.vision." + targetElement.getSimpleName().toString()
    // + "Router";
    // JavaFileObject jfoRouter = filer.createSourceFile(routerClassName, targetElement);
    // Writer writerRouter = jfoRouter.openWriter();
    // JavaWriter javaWriterRouter = new JavaWriter(writerRouter);
    //
    // javaWriterRouter.emitPackage("com.nurun.persistence.temp.vision");
    // javaWriterRouter.emitImports(targetElement.toString());
    // javaWriterRouter.emitImports("com.nurun.persistence.temp.vision.ContentUriRouter");
    // javaWriterRouter.emitEmptyLine();
    // javaWriterRouter.beginType(routerClassName, "class", EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT), null,
    // "ContentUriRouter");
    // javaWriterRouter.emitEmptyLine();
    //
    // javaWriterRouter.beginMethod(null, routerClassName, EnumSet.of(Modifier.PUBLIC), targetElement
    // .getSimpleName().toString(), "target");
    // javaWriterRouter.endMethod();
    //
    // for (ExecutableElement method : metadata.getMethodsForClass(targetElement)) {
    //
    // // List<? extends VariableElement> methodParams = method.getParameters();
    // // String[] paramsAndTypesArray = new String[methodParams.size() * 2];
    // // List<String> paramsAndTypesList = new ArrayList<String>(methodParams.size() * 2);
    // //
    // // for (VariableElement param : methodParams) {
    // // paramsAndTypesList.add(param.asType().toString());
    // // paramsAndTypesList.add(param.getSimpleName().toString());
    // // }
    // //
    // // javaWriterContentUriRegistry.beginMethod("void", method.getSimpleName().toString(),
    // // method.getModifiers(), paramsAndTypesList.toArray(paramsAndTypesArray)).endMethod();
    // }
    //
    // closeContentUriRegistry(writerRouter, javaWriterRouter);
    // }
    //
    // closeContentUriRegistry(writerContentUriRegistry, javaWriterContentUriRegistry);
    // }

    // private void writeContentUriRegistryPackage(JavaWriter javaWriterContentUriRegistry) throws IOException {
    // javaWriterContentUriRegistry.emitPackage("com.nurun.persistence.temp.vision");
    // }
    //
    // private void writeContentUriRegistryImports(Metadata metadata, JavaWriter javaWriterContentUriRegistry)
    // throws IOException {
    //
    // for (TypeElement targetElement : metadata.getTargetClasses()) {
    // javaWriterContentUriRegistry.emitImports(targetElement.toString());
    // }
    //
    // javaWriterContentUriRegistry.emitEmptyLine();
    // javaWriterContentUriRegistry.emitImports("android.content.UriMatcher");
    // javaWriterContentUriRegistry.emitEmptyLine();
    // }
    //
    // private void writeContentUriRegistryBeginClass(JavaWriter javaWriterContentUriRegistry) throws IOException {
    // javaWriterContentUriRegistry.beginType("com.nurun.persistence.temp.vision.ContentUriRegistry", "class",
    // EnumSet.of(Modifier.PUBLIC));
    // javaWriterContentUriRegistry.emitEmptyLine();
    // }
    //
    // private void writeContentUriRegistryUriMatcherInitializer(JavaWriter javaWriterContentUriRegistry)
    // throws IOException {
    // Set<Modifier> modifiers = new HashSet<Modifier>();
    // modifiers.add(Modifier.STATIC);
    // modifiers.add(Modifier.FINAL);
    // javaWriterContentUriRegistry.emitField("UriMatcher", "URI_MATCHER", modifiers);
    // javaWriterContentUriRegistry.emitEmptyLine();
    // javaWriterContentUriRegistry.beginInitializer(true);
    // javaWriterContentUriRegistry.emitStatement("URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH)");
    // javaWriterContentUriRegistry.emitEmptyLine();
    //
    // Map<String, Set<String>> authoritiesAndUris = metadata.getAuthoritiesAndUris();
    // int uriId = 0;
    //
    // for (String authority : authoritiesAndUris.keySet()) {
    //
    // Set<String> uris = authoritiesAndUris.get(authority);
    //
    // for (String uri : uris) {
    // javaWriterContentUriRegistry.emitStatement("URI_MATCHER.addURI(\"%s\", \"%s\", %d)", authority, uri,
    // ++uriId);
    // }
    // }
    //
    // javaWriterContentUriRegistry.endInitializer();
    // }
    //
    // private void writeContentUriRegistryGetRoutForMethodSignature(JavaWriter javaWriterContentUriRegistry)
    // throws IOException {
    //
    // Set<Modifier> modifiers = new HashSet<Modifier>();
    // modifiers.add(Modifier.PUBLIC);
    // modifiers.add(Modifier.STATIC);
    //
    // javaWriterContentUriRegistry.emitEmptyLine();
    // javaWriterContentUriRegistry.beginMethod("ContentUriRouter", "getRouterFor", modifiers, "Object", "object");
    // javaWriterContentUriRegistry.emitEmptyLine();
    // javaWriterContentUriRegistry.emitStatement("Class<?> objectClass = object.getClass()");
    // javaWriterContentUriRegistry.emitEmptyLine();
    //
    // for (TypeElement annotatedType : metadata.getTargetClasses()) {
    //
    // javaWriterContentUriRegistry.beginControlFlow(String.format("if (objectClass == %s.class)",
    // annotatedType.getSimpleName()));
    // javaWriterContentUriRegistry.emitSingleLineComment("return new %sRouter((%s) object)",
    // annotatedType.getSimpleName(), annotatedType.getSimpleName());
    // javaWriterContentUriRegistry.endControlFlow();
    // javaWriterContentUriRegistry.emitEmptyLine();
    // }
    //
    // javaWriterContentUriRegistry.emitStatement("return null");
    // javaWriterContentUriRegistry.endMethod();
    // }
    //
    // private void closeContentUriRegistry(Writer writerContentUriRegistry, JavaWriter javaWriterContentUriRegistry)
    // throws IOException {
    // javaWriterContentUriRegistry.endType();
    // javaWriterContentUriRegistry.close();
    // writerContentUriRegistry.close();
    // }

    private boolean isAbstract(Element element) {

        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    private boolean isClassOrInterface(Element element) {

        return element.getKind().equals(ElementKind.CLASS) || element.getKind().equals(ElementKind.INTERFACE);
    }

    private boolean isInterface(Element element) {

        return element.getKind().equals(ElementKind.INTERFACE);
    }

    private boolean isClass(Element element) {

        return element.getKind().equals(ElementKind.CLASS);
    }

    /**
     * @param string
     */
    private void debug(Object string) {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[DEBUG***] - " + string);
    }
}
