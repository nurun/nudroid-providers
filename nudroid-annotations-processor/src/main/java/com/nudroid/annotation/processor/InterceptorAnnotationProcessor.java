package com.nudroid.annotation.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

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

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    InterceptorAnnotationProcessor(ProcessorContext processorContext) {

        this.mLogger = processorContext.logger;
        this.mTypeUtils = processorContext.typeUtils;
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

        Set<Interceptor> interceptors = new HashSet<Interceptor>();

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

                        mLogger.error(String.format("    Only one interceptor class for annotation %s is supported."
                                + " Found multiple interceptors: %s", interceptorAnnotation, interceptorClassSet),
                                interceptorClass);
                    }

                    continue;
                }

                if (interceptorClassSet.size() == 1) {

                    interceptors.add(new Interceptor((TypeElement) interceptorAnnotation, interceptorClassSet
                            .iterator().next()));
                }
            }
        }

        processInterceptorAnnotation(interceptors, metadata);
        mLogger.info(String.format("Done processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
    }

    private void processInterceptorAnnotation(Set<Interceptor> interceptors, Metadata metadata) {

        for (DelegateClass delagateClass : metadata.getDelegateClasses()) {

            for (DelegateMethod delegateMethod : delagateClass.getDelegateMethods()) {

                mLogger.trace("    Processing method " + delegateMethod.getName());
                ExecutableElement executableElement = delegateMethod.getExecutableElement();

                List<? extends AnnotationMirror> annotationMirrors = executableElement.getAnnotationMirrors();
                mLogger.trace("        Annotations on method: " + annotationMirrors);

                for (AnnotationMirror mirror : annotationMirrors) {

                    for (Interceptor interceptor : interceptors) {

                        if (mTypeUtils.asElement(mirror.getAnnotationType()).equals(
                                interceptor.getInterceptorAnnotationElement())) {

                            mLogger.trace(String.format("        Added interceptor %s to method.",
                                    interceptor.getSimpleName()));
                            delegateMethod.addInterceptor(interceptor);
                        }
                    }
                }

                mLogger.trace("    Done processing method " + delegateMethod.getName());
            }
        }
    }
}