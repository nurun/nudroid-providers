package com.nudroid.persistence.annotation.processor;

import java.io.IOException;
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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	static final String NUDROID_PERSISTENCE_BASE_PACKAGE = PersistenceAnnotationProcessor.class.getPackage().getName();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Elements elementUtils;
	private Types typeUtils;
	private Filer filer;
	private boolean initialized = false;
	private int iterationRun = 0;

	private ProcessorContinuation continuation;

	@Override
	public synchronized void init(ProcessingEnvironment env) {

		super.init(env);

		logger.info("Initializing nudroid persistence annotation processor.");

		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();

		continuation = new ProcessorContinuation(filer);

		try {
			continuation.loadContinuation();
			initialized = true;
			logger.info("Initialization complete.");
		} catch (IOException e) {
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

			logger.info("[{}] Processing root elements: {}", iterationRun, roundEnv.getRootElements());

			Set<Element> elementsToProcess = getElementsToProcess(roundEnv);

			for (Element element : elementsToProcess) {

				processQuery(element, continuation);
			}

			logger.info("TEST");
		}

		//
		// Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Query.class);
		//
		// for (Element elem : elements) {
		// processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[DEBUG***] - ", elem);
		// }

		// try {
		//
		// FileObject resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, "com.nudroid.annotation.processor",
		// "index.index");
		// File file = new File(
		// URI.create("file:///Users/daniel.freitas/dev/workspace/testpersistence/generated/com/nudroid/annotation/processor/index.index"));
		//
		// Scanner s = new Scanner(file);
		//
		// Writer writer = resource.openWriter();
		// PrintWriter printWriter = new PrintWriter(writer);
		// printWriter.println("Replaced");
		// printWriter.close();
		// writer.close();
		//
		// s.close();
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }
		//
		// if (firstRun) {
		//
		// indexedTypes = createIndexedTypeSet(metadata.getIndexedTypes());
		// }
		//
		// for (Element element : roundEnv.getRootElements()) {
		//
		// if (element instanceof TypeElement) {
		//
		// indexedTypes.add((TypeElement) element);
		// }
		// }
		//
		// try {
		// FileObject indexFile = filer.createResource(StandardLocation.SOURCE_OUTPUT,
		// "com.nudroid.annotation.processor", "index.index");
		// Writer writer = indexFile.openWriter();
		// PrintWriter printWriter = new PrintWriter(writer);
		//
		// for (TypeElement element : indexedTypes) {
		// printWriter.println(element.toString());
		// }
		//
		// printWriter.close();
		// writer.close();
		// } catch (IOException e) {
		// debug("Error " + e);
		// }
		//
		// for (Element elem : roundEnv.getElementsAnnotatedWith(Query.class)) {
		//
		// Element enclosingElement = elem.getEnclosingElement();
		// Element parentEnclosingElement = enclosingElement.getEnclosingElement();
		// Set<Modifier> modifiers = enclosingElement.getModifiers();
		//
		// if (!validateClassIsTopLevelOrStatic(elem, enclosingElement, parentEnclosingElement, modifiers)) {
		// continue;
		// }
		//
		// if (!validateClassHasDefaultConstructor(enclosingElement)) {
		// continue;
		// }
		//
		// Authority authority = enclosingElement.getAnnotation(Authority.class);
		// String authorityString = authority != null ? authority.value() : enclosingElement.toString();
		//
		// try {
		// TypeElement annotatedType = metadata.getClassForAuthority(authorityString);
		//
		// if (annotatedType == null || annotatedType.equals(enclosingElement)) {
		//
		// metadata.addClassAuthority(authorityString, (TypeElement) enclosingElement);
		// } else {
		//
		// processingEnv.getMessager().printMessage(
		// Diagnostic.Kind.WARNING,
		// String.format("Class %s already defines an authority named '%s'.", annotatedType,
		// authority.value()), enclosingElement);
		// }
		//
		// metadata.addUri(authorityString, elem.getAnnotation(Query.class).value());
		//
		// } catch (DuplicateUriParameterException e) {
		//
		// processingEnv.getMessager().printMessage(
		// Diagnostic.Kind.ERROR,
		// String.format("Duplicated parameter. Parameter '%s', appearing at position '%d' "
		// + "is already present at position '%d'.", e.getParamName(), e.getDuplicatePosition(),
		// e.getExistingPosition()), elem);
		// }
		//
		// metadata.addTargetMethod((TypeElement) enclosingElement, (ExecutableElement) elem);
		// }
		//
		// if (firstRun) {
		//
		// // try {
		// // generateCompanionSourceCode(metadata);
		// generateCompanionSourceCode2(metadata);
		// // } catch (IOException e) {
		// // e.printStackTrace();
		// // processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
		// // "Unable to create companion class source code. Exception below.");
		// // }
		// }

		return true;
	}

	private void processQuery(Element element, ProcessorContinuation continuation2) {

		if (element.getKind() != ElementKind.CLASS) {
			return;
		}

		List<? extends Element> methodElementsList = element.getEnclosedElements();

		for (Element targetElement : methodElementsList) {

			if (!(targetElement instanceof ExecutableElement)) continue;

			Query query = targetElement.getAnnotation(Query.class);

			if (query == null) continue;

			// TODO Get query data and do the mapping.
		}
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

	private boolean isFirstRun() {

		return iterationRun == 1;
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
			        .createSourceFile("com.nurun.persistence.annotation.ContentUriRegistry");
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

		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[DEBUG***] - " + string);
	}
}
