package com.nudroid.annotation.processor;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateUri;
import com.nudroid.annotation.processor.model.MatcherUri;
import com.nudroid.annotation.provider.delegate.ContentProviderDelegate;
import com.nudroid.annotation.provider.delegate.Query;

/**
 * Processes the Query annotations on a class.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class QueryAnnotationProcessor {

    private Continuation continuation;
    private Metadata metadata;
    private ProcessingEnvironment processingEnv;
    private LoggingUtils logger;

    private TypeMirror stringType;
    private TypeMirror arrayOfStringsType;
    private TypeMirror contentValuesType;
    private TypeMirror uriType;
    private Types typeUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    QueryAnnotationProcessor(ProcessorContext processorContext) {

        this.metadata = processorContext.metadata;
        this.processingEnv = processorContext.processingEnv;
        this.typeUtils = processorContext.typeUtils;
        this.logger = processorContext.logger;

        stringType = processorContext.elementUtils.getTypeElement("java.lang.String").asType();
        arrayOfStringsType = processorContext.typeUtils.getArrayType(stringType);
        contentValuesType = processorContext.elementUtils.getTypeElement("android.content.ContentValues").asType();
        uriType = processorContext.elementUtils.getTypeElement("android.net.Uri").asType();
    }

    /**
     * Process the {@link Query} annotations on this round.
     * 
     * @param roundEnv
     *            The round environment to process.
     * @param metadata
     *            The annotation metadata for the processor.
     */
    @SuppressWarnings("unchecked")
    public void processQueryAnnotationOnMethods(RoundEnvironment roundEnv, Metadata metadata) {

        Set<ExecutableElement> queryMethods = (Set<ExecutableElement>) roundEnv.getElementsAnnotatedWith(Query.class);

        for (ExecutableElement queryMethod : queryMethods) {

            TypeElement enclosingClass = (TypeElement) queryMethod.getEnclosingElement();
            DelegateClass delegateClass = metadata.getDelegateClassForTypeElement(enclosingClass);

            if (delegateClass == null) {
                logger.error(
                        String.format("Enclosing class must be annotated wirh @%s",
                                ContentProviderDelegate.class.getName()), queryMethod);
            } else {

                processQueryOnMethod(enclosingClass, queryMethod, delegateClass);
            }
        }
    }

    private void processQueryOnMethod(TypeElement enclosingClass, ExecutableElement queryMethod,
            DelegateClass delegateClass) {

        Query query = queryMethod.getAnnotation(Query.class);
        String pathAndQuery = query.value();

        DelegateUri delegateUri = delegateClass.registerPath(queryMethod, pathAndQuery);

        DelegateMethod delegateMethod = new DelegateMethod(queryMethod, delegateUri);

        boolean isValidAnnotations = validateAnnotations(enclosingClass, queryMethod, query, uri);

        if (!isValidAnnotations || ElementUtils.isAbstract(enclosingClass)) return;

        logger.debug(String.format("Processing Query annotation on %s.%s", enclosingClass, queryMethod));
        // metadata.mapUri(rootClass, (ExecutableElement) method, uri);
    }
}
