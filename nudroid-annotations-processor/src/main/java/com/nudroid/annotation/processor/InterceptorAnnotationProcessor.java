package com.nudroid.annotation.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.nudroid.annotation.processor.model.AnnotationAttribute;
import com.nudroid.annotation.processor.model.ConcreteAnnotation;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateMethod;
import com.nudroid.annotation.processor.model.Interceptor;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint;

/**
 * TODO: Add validation to interceptors.
 * <p/>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class InterceptorAnnotationProcessor {

    private LoggingUtils mLogger;
    private Types mTypeUtils;
    private Elements mElementUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    InterceptorAnnotationProcessor(ProcessorContext processorContext) {

        this.mLogger = processorContext.logger;
        this.mTypeUtils = processorContext.typeUtils;
        this.mElementUtils = processorContext.elementUtils;
    }

    /**
     * Process the {@link Query} annotations on this round.
     * 
     * @param roundEnv
     *            The round environment to process.
     * @param metadata
     *            The annotation metadata for the processor.
     * @param continuation
     *            The continuation object for this processor.
     */
    void process(RoundEnvironment roundEnv, Metadata metadata, Continuation continuation) {

        /*
         * Do not assume that because the @ProviderInterceptorPoint annotation can only be applied to annotation types,
         * only TypeElements will be returned. Compilation errors on a class can let the compiler think the annotation
         * is applied to other elements even if it is correctly applied to a class, causing a class cast exception in
         * the for loop below.
         */
        Set<Element> interceptorAnnotations = new HashSet<Element>();
        interceptorAnnotations.addAll(roundEnv.getElementsAnnotatedWith(ProviderInterceptorPoint.class));
        interceptorAnnotations.addAll(continuation.getInterceptorAnnotations());

        mLogger.info(String.format("Start processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
        mLogger.trace(String.format("    Interfaces annotated with @%s for the round: %s",
                ProviderInterceptorPoint.class.getSimpleName(), interceptorAnnotations));

        for (Element interceptorAnnotation : roundEnv.getElementsAnnotatedWith(ProviderInterceptorPoint.class)) {

            createAnnotationMetadata(interceptorAnnotation, metadata);
        }

        for (Element interceptorAnnotation : interceptorAnnotations) {

            if (interceptorAnnotation instanceof TypeElement) {

                Set<? extends Element> elementsAnnotatedWithInterceptor = continuation.getElementsAnotatedWith(
                        (TypeElement) interceptorAnnotation, roundEnv);
                Set<TypeElement> interceptorClassSet = ElementFilter.typesIn(elementsAnnotatedWithInterceptor);
                continuation.addInterceptorAnnotation((TypeElement) interceptorAnnotation);
                continuation.addInterceptorClasses(interceptorClassSet);

                mLogger.trace(String.format("    Interceptor classes for %s: %s", interceptorAnnotation,
                        interceptorClassSet));

                if (interceptorClassSet.size() > 1) {
                    mLogger.trace(String.format(
                            "    Multiple interceptors for annotation %s. Signaling compilatoin error.",
                            interceptorAnnotation));

                    for (TypeElement interceptorClass : interceptorClassSet) {

                        mLogger.error(String.format("Only one interceptor class for annotation %s is supported."
                                + " Found multiple interceptors: %s", interceptorAnnotation, interceptorClassSet),
                                interceptorClass);
                    }

                    continue;
                }

                if (interceptorClassSet.size() == 1) {

                    final TypeElement interceptorClass = interceptorClassSet.iterator().next();

                    processInterceptorAnnotation2(metadata, (TypeElement) interceptorAnnotation, interceptorClass);
                }
            }
        }

        mLogger.info(String.format("Done processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
    }

    private void createAnnotationMetadata(Element interceptorAnnotation, Metadata metadata) {

        if (interceptorAnnotation instanceof TypeElement) {

            ConcreteAnnotation annotation = new ConcreteAnnotation((TypeElement) interceptorAnnotation);

            for (Element method : interceptorAnnotation.getEnclosedElements()) {

                if (method instanceof ExecutableElement) {
                    annotation.addAttribute(new AnnotationAttribute((ExecutableElement) method));
                }
            }

            metadata.registerConcreteAnnotation(annotation);
        }
    }

    private void processInterceptorAnnotation2(Metadata metadata, TypeElement interceptorAnnotation,
            TypeElement interceptorClass) {

        for (DelegateClass delagateClass : metadata.getDelegateClasses()) {

            for (DelegateMethod delegateMethod : delagateClass.getDelegateMethods()) {

                mLogger.trace("    Processing method " + delegateMethod.getName());
                ExecutableElement executableElement = delegateMethod.getExecutableElement();

                List<? extends AnnotationMirror> annotationsMirrors = executableElement.getAnnotationMirrors();

                for (AnnotationMirror mirror : annotationsMirrors) {

                    if (mTypeUtils.isSameType(mirror.getAnnotationType(), interceptorAnnotation.asType())) {

                        Interceptor interceptor = new Interceptor(interceptorAnnotation, interceptorClass, mTypeUtils,
                                metadata.getConcreteAnnotation(interceptorAnnotation));

                        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = mElementUtils
                                .getElementValuesWithDefaults(mirror);

                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationValues
                                .entrySet()) {

                            interceptor.addAnnotationValue(entry.getKey(), entry.getValue(), mElementUtils, mTypeUtils);
                        }

                        delegateMethod.addInterceptor(interceptor);
                    }
                }

                mLogger.trace("    Done processing method " + delegateMethod.getName());
            }
        }
    }
}
