/**
 * 
 */
package com.nudroid.annotation.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
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
import javax.lang.model.util.Types;
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

import com.nurun.persistence.temp.vision.Authority;
import com.nurun.persistence.temp.vision.Query;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
@SupportedAnnotationTypes({ "com.nurun.persistence.temp.vision.Authority", "com.nurun.persistence.temp.vision.Delete",
        "com.nurun.persistence.temp.vision.Insert", "com.nurun.persistence.temp.vision.Projection",
        "com.nurun.persistence.temp.vision.Query", "com.nurun.persistence.temp.vision.QueryParam",
        "com.nurun.persistence.temp.vision.Selection", "com.nurun.persistence.temp.vision.SelectionArgs",
        "com.nurun.persistence.temp.vision.SortOrder", "com.nurun.persistence.temp.vision.Update" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PersistenceProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Metadata metadata = new Metadata();
    private boolean firstRun;
    private Set<TypeElement> indexedTypes;
    private RoundEnvironment roundEnv;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        firstRun = true;
        metadata.init();
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

        this.roundEnv = roundEnv;

        try {

            FileObject resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, "com.nudroid.annotation.processor",
                    "index.index");
            File file = new File(
                    URI.create("file:///Users/daniel.freitas/dev/workspace/testpersistence/generated/com/nudroid/annotation/processor/index.index"));

            debug("--00File path is " + file);
            debug("Got res. file exists? " + file.exists());
            Scanner s = new Scanner(file);
            debug("Trying to read line");
            debug("zzTOP" + s.nextLine());

            Writer writer = resource.openWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            printWriter.println("Replaced");
            printWriter.close();
            writer.close();

            debug("File replaced...");
            
            s.close();
        } catch (Exception e1) {
            debug("======*****Could not load resource" + e1 + ":" + e1.getMessage());
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (firstRun) {

            indexedTypes = createIndexedTypeSet(metadata.getIndexedTypes());
        }

        for (Element element : roundEnv.getRootElements()) {

            if (element instanceof TypeElement) {

                indexedTypes.add((TypeElement) element);
            }
        }

        try {
            FileObject indexFile = filer.createResource(StandardLocation.SOURCE_OUTPUT,
                    "com.nudroid.annotation.processor", "index.index");
            Writer writer = indexFile.openWriter();
            PrintWriter printWriter = new PrintWriter(writer);

            for (TypeElement element : indexedTypes) {
                printWriter.println(element.toString());
            }

            printWriter.close();
            writer.close();
        } catch (IOException e) {
            debug("Error " + e);
        }

        for (Element elem : roundEnv.getElementsAnnotatedWith(Query.class)) {

            Element enclosingElement = elem.getEnclosingElement();
            Element parentEnclosingElement = enclosingElement.getEnclosingElement();
            Set<Modifier> modifiers = enclosingElement.getModifiers();

            if (!validateClassIsTopLevelOrStatic(elem, enclosingElement, parentEnclosingElement, modifiers)) {
                continue;
            }

            if (!validateClassHasDefaultConstructor(enclosingElement)) {
                continue;
            }

            Authority authority = enclosingElement.getAnnotation(Authority.class);
            String authorityString = authority != null ? authority.value() : enclosingElement.toString();

            try {
                TypeElement annotatedType = metadata.getClassForAuthority(authorityString);

                if (annotatedType == null || annotatedType.equals(enclosingElement)) {

                    metadata.addClassAuthority(authorityString, (TypeElement) enclosingElement);
                } else {

                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.WARNING,
                            String.format("Class %s already defines an authority named '%s'.", annotatedType,
                                    authority.value()), enclosingElement);
                }

                metadata.addUri(authorityString, elem.getAnnotation(Query.class).value());

            } catch (DuplicateUriParameterException e) {

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Duplicated parameter. Parameter '%s', appearing at position '%d' "
                                + "is already present at position '%d'.", e.getParamName(), e.getDuplicatePosition(),
                                e.getExistingPosition()), elem);
            }

            metadata.addTargetMethod((TypeElement) enclosingElement, (ExecutableElement) elem);
        }

        if (firstRun) {

            // try {
            // generateCompanionSourceCode(metadata);
            generateCompanionSourceCode2(metadata);
            // } catch (IOException e) {
            // e.printStackTrace();
            // processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            // "Unable to create companion class source code. Exception below.");
            // }
        }

        firstRun = false;

        return true;
    }

    private Set<TypeElement> createIndexedTypeSet(Set<String> indexedTypes) {

        HashSet<TypeElement> elements = new HashSet<TypeElement>();

        for (String className : indexedTypes) {

            TypeElement typeElement = elementUtils.getTypeElement(className);

            if (typeElement != null) {
                elements.add(typeElement);
            }
        }

        return elements;
    }

    private void generateCompanionSourceCode2(Metadata metadata) {

        Properties p = new Properties();
        p.put("resource.loader", "classpath");
        p.put("classpath.resource.loader.description", "Velocity Classpath Resource Loader");
        p.put("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put("contentProviders", metadata.getTargetClasses());
        context.put("authoritiesAndUris", metadata.getAuthoritiesAndUris());

        Template template = null;

        try {
            template = Velocity.getTemplate("com/nudroid/annotation/processor/ContentUriRegistry.vm");
            JavaFileObject jfoContentUriRegistry = filer
                    .createSourceFile("com.nurun.persistence.temp.vision.ContentUriRegistry");
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

    private boolean validateClassIsTopLevelOrStatic(Element elem, Element enclosingElement,
            Element parentEnclosingElement, Set<Modifier> modifiers) {

        if (!enclosingElement.getKind().equals(ElementKind.CLASS)
                || (parentEnclosingElement.getKind().equals(ElementKind.CLASS) && !modifiers.contains(Modifier.STATIC))) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Query annotations can only appear on top level or static classes.", elem);

            return false;
        }

        return true;
    }

    private boolean validateClassHasDefaultConstructor(Element enclosingElement) {

        List<? extends Element> enclosedElements = enclosingElement.getEnclosedElements();

        for (Element child : enclosedElements) {

            if (child.getKind().equals(ElementKind.CONSTRUCTOR)) {

                if (((ExecutableElement) child).getParameters().size() == 0) {
                    return true;
                }
            }
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Classes annotated with content provider query annotations must provide a default constructor.",
                enclosingElement);

        return false;
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

    /**
     * @param string
     */
    private void debug(Object string) {

        Set<? extends Element> elems = roundEnv.getRootElements();

        if (!elems.isEmpty()) {

            for (Element e : elems) {

                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[DEBUG***] - " + string, e);
                return;
            }

        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[DEBUG***] - " + string);
        }
    }
}
