package com.nudroid.persistence.annotation.processor;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.nurun.persistence.annotation.Authority;
import com.nurun.persistence.annotation.ContentUri;
import com.nurun.persistence.annotation.ContentValues;
import com.nurun.persistence.annotation.PathParam;
import com.nurun.persistence.annotation.Projection;
import com.nurun.persistence.annotation.Query;
import com.nurun.persistence.annotation.QueryParam;
import com.nurun.persistence.annotation.Selection;
import com.nurun.persistence.annotation.SelectionArgs;
import com.nurun.persistence.annotation.SortOrder;

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
@SupportedOptions({ "persistence.annotation.log.level" })
public class PersistenceAnnotationProcessor extends AbstractProcessor {

    static final String GENERATED_CODE_BASE_PACKAGE = "com.nudroid.persistence";
    static final String CONTENT_URI_REGISTRY_CLASS_NAME = "ContentUriRegistry";

    private static final String CONTENT_URI_REGISTRY_TEMPLATE_LOCATION = "com/nudroid/persistence/annotation/processor/ContentUriRegistryTemplate.vm";
    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION = "com/nudroid/persistence/annotation/processor/RouterTemplate.vm";

    private LoggingUtils logger;

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private boolean initialized = false;
    private int iterationRun = 0;

    private Continuation continuation;
    private UriRegistry uriRegistry = new UriRegistry();
    private Metadata metadata;

    private TypeMirror stringType;
    private TypeMirror arrayOfStringsType;
    private TypeMirror contentValuesType;
    private TypeMirror uriType;

    @Override
    public synchronized void init(ProcessingEnvironment env) {

        super.init(env);

        String logLevel = env.getOptions().get("persistence.annotation.log.level");

        if (logLevel == null) {
            logLevel = System.getProperty("persistence.annotation.log.level");
        }

        logger = new LoggingUtils(env.getMessager(), logLevel);

        logger.debug("Initializing nudroid persistence annotation processor.");

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        stringType = elementUtils.getTypeElement("java.lang.String").asType();
        arrayOfStringsType = typeUtils.getArrayType(stringType);
        contentValuesType = elementUtils.getTypeElement("android.content.ContentValues").asType();
        uriType = elementUtils.getTypeElement("android.net.Uri").asType();

        continuation = new Continuation(filer, logger);
        metadata = new Metadata(uriRegistry, elementUtils, typeUtils, logger);

        try {
            continuation.loadContinuation();
            initialized = true;
            logger.debug("Initialization complete.");
        } catch (Exception e) {
            logger.error("Unable to load continuation index file. Aborting annotation processor: " + e.getMessage());
            throw new AnnotationProcessorError(e);
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

        logger.info("Start processing " + roundEnv.getRootElements());
        
        iterationRun++;

        if (!initialized) {

            logger.error("Annotation processor not initialized. Aborting.");

            return false;
        }

        if (isFirstRun()) {

            Set<Element> elementsToProcess = getElementsToProcess(roundEnv);

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
        logger.debug(String.format("Root elements being porocessed this round: %s", classesToProcess));
        Set<? extends Element> previousDelegateElements = continuation.loadDelegateElements(elementUtils);

        previousDelegateElements.removeAll(classesToProcess);

        if (!previousDelegateElements.isEmpty()) {

            logger.debug(String.format("Adding continuation elements from previous builds: %s",
                    previousDelegateElements));
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

        boolean isValid = true;

        if (isInterface(rootClass)) {

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "Java does not inherit annotations from methods. "
                            + "The @Query annotation in this method won't be inherited by implementing classes.",
                    method);

            isValid = false;
        }

        if (isAbstract(rootClass)) {

            return;
        }

        if (!validateQueryElement(rootClass, method)) isValid = false;
        if (!validateParameters(query, (ExecutableElement) method)) isValid = false;
        if (!isValid) return;

        logger.debug(String.format("Processing Query annotation on %s.%s", rootClass, method));

        try {

            metadata.mapUri(rootClass, (ExecutableElement) method, metadata.parseAuthorityFromClass(rootClass), query);

        } catch (DuplicateUriParameterException e) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("Duplicated placeholder parameter: '%s'", e.getParamName()), method);
        } catch (IllegalUriPathException e) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("URL '%s' is not valid.", query.value()), method);
        }
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

    private boolean validateParameters(Query query, ExecutableElement method) {

        boolean isValid = true;

        List<? extends VariableElement> parameters = method.getParameters();

        for (VariableElement var : parameters) {

            List<Class<?>> accumulatedAnnotations = new ArrayList<Class<?>>();

            final TypeMirror parameterType = var.asType();

            isValid = validateParameterAnnotation(var, Projection.class, parameterType, arrayOfStringsType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, Selection.class, parameterType, stringType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, SelectionArgs.class, parameterType, arrayOfStringsType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, SortOrder.class, parameterType, stringType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, ContentValues.class, parameterType, contentValuesType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, ContentUri.class, parameterType, uriType, accumulatedAnnotations);

            isValid = validatePathParamAnnotation(var, query, accumulatedAnnotations);
            isValid = validateQueryParamAnnotation(var, query, accumulatedAnnotations);
        }

        return isValid;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean validateParameterAnnotation(VariableElement var, Class annotationClass, TypeMirror parameterType,
            TypeMirror requiredType, List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        if (var.getAnnotation(annotationClass) != null) {

            accumulatedAnnotations.add(annotationClass);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s can only be annotated with one of %s.", var,
                                accumulatedAnnotations), var);
            }

            if (!typeUtils.isSameType(parameterType, requiredType)) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s annotated with %s must be of type %s.", var, annotationClass,
                                requiredType), var);
            }
        }

        return isValid;
    }

    private boolean validatePathParamAnnotation(VariableElement var, Query query, List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        final PathParam annotation = var.getAnnotation(PathParam.class);

        if (annotation != null) {

            String paramName = annotation.value();
            final String queryPath = query.value();
            Uri uri = null;

            try {
                uri = new Uri("bidon", queryPath, logger);
            } catch (IllegalUriPathException e) {

                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        String.format("Path '%s' is not a valid URI path and query string combination.", queryPath),
                        var);

                return false;
            }

            accumulatedAnnotations.add(PathParam.class);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s can only be annotated with one of %s.", var,
                                accumulatedAnnotations), var);
            }

            if (!uri.containsPathPlaceholder(paramName)) {

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Could not find placeholder named '%s' on the path element of '%s'.", paramName,
                                queryPath), var);

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateQueryParamAnnotation(VariableElement var, Query query, List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        final QueryParam annotation = var.getAnnotation(QueryParam.class);

        if (annotation != null) {

            String paramName = annotation.value();
            final String queryPath = query.value();
            Uri uri = null;

            try {
                uri = new Uri("biddon", queryPath, logger);
            } catch (IllegalUriPathException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        String.format("Path '%s' is not a valid URI path and query string combination.", queryPath),
                        var);

                return false;
            }

            accumulatedAnnotations.add(PathParam.class);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s can only be annotated with one of %s.", var,
                                accumulatedAnnotations), var);
            }

            if (!uri.containsQueryPlaceholder(paramName)) {

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Could not find placeholder named '%s' on the query string element of '%s'.",
                                paramName, queryPath), var);

                isValid = false;
            }
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

        generateContentUriRegistry(metadata);
        generateContentProviderRouters(metadata);
    }

    private void generateContentUriRegistry(Metadata metadata) {

        logger.debug(String.format("Generating ContentUriRegistry source code at %s.%s", GENERATED_CODE_BASE_PACKAGE,
                CONTENT_URI_REGISTRY_CLASS_NAME));

        Properties p = generateVelocityConfigurationProperties();
        Velocity.init(p);
        logger.debug(String.format("Configured velocity properties with %s.", p));

        VelocityContext context = new VelocityContext();
        context.put("delegateClasses", metadata.getDelegateClasses().values());
        context.put("contentProviderUris", uriRegistry.getUniqueUris());
        logger.debug(String.format("Configured velocity context."));

        Template template = null;

        try {
            template = Velocity.getTemplate(CONTENT_URI_REGISTRY_TEMPLATE_LOCATION);
            logger.debug(String.format("Successfully loaded velocity template %s.",
                    CONTENT_URI_REGISTRY_TEMPLATE_LOCATION));

            JavaFileObject jfoContentUriRegistry = filer.createSourceFile(String.format("%s.%s",
                    GENERATED_CODE_BASE_PACKAGE, CONTENT_URI_REGISTRY_CLASS_NAME));
            logger.debug(String.format("Successfully created JavaFileObject for %s.%s.", GENERATED_CODE_BASE_PACKAGE,
                    CONTENT_URI_REGISTRY_CLASS_NAME));

            Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();
            logger.debug(String.format("Successfully openned JavaFileObject Writer."));

            template.merge(context, writerContentUriRegistry);
            logger.debug(String.format("Successfully merged velocity template."));

            writerContentUriRegistry.close();
            logger.debug(String.format("Successfully closed writer."));

            logger.debug(String.format("Source code generation for %s.%s is done.", GENERATED_CODE_BASE_PACKAGE,
                    CONTENT_URI_REGISTRY_CLASS_NAME));
        } catch (Exception e) {
            logger.error(String.format("Error processing velocity script '%s'", CONTENT_URI_REGISTRY_TEMPLATE_LOCATION));
            throw new AnnotationProcessorError(e);
        }
    }

    private void generateContentProviderRouters(Metadata metadata) {

        for (DelegateClass delegateClass : metadata.getDelegateClasses().values()) {
            Properties p = generateVelocityConfigurationProperties();
            Velocity.init(p);
            VelocityContext context = new VelocityContext();
            context.put("delegateClass", delegateClass);
            context.put("classUriIds", delegateClass.getUriIds());
            context.put("delegateMethods", metadata.getDelegateMethods());

            Template template = null;

            try {
                template = Velocity.getTemplate(CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION);
                JavaFileObject jfoContentUriRegistry = filer.createSourceFile(String.format("%s.%s",
                        GENERATED_CODE_BASE_PACKAGE, delegateClass.getRouterName()));
                Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();

                template.merge(context, writerContentUriRegistry);
                writerContentUriRegistry.close();
            } catch (Exception e) {
                logger.error(String.format("Error processing velocity script '%s'",
                        CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION));
                throw new AnnotationProcessorError(e);
            }
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
                    Continuation.CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME);
            Writer writer = indexFile.openWriter();
            PrintWriter printWriter = new PrintWriter(writer);

            for (String indexedTypeName : indexedTypes) {
                printWriter.println(indexedTypeName);
            }

            printWriter.close();
            writer.close();
        } catch (Exception e) {
            logger.error(String.format("Error processing continuation index file '%s.%s'", GENERATED_CODE_BASE_PACKAGE,
                    Continuation.CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME));
            throw new AnnotationProcessorError(e);
        }
    }

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
}
