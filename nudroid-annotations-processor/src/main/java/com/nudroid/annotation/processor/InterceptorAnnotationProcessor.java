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
 * Processes interceptor annotations annotations on delegate methods.
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
     */
    @SuppressWarnings("unchecked")
    public void process(RoundEnvironment roundEnv, Metadata metadata) {

        Set<TypeElement> interceptorAnnotations = (Set<TypeElement>) roundEnv
                .getElementsAnnotatedWith(ProviderInterceptorPoint.class);

        mLogger.info(String.format("Start processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
        mLogger.trace(String.format("    Interfaces annotated with @%s for the round: %s",
                ProviderInterceptorPoint.class.getSimpleName(), interceptorAnnotations));

        Set<Interceptor> interceptors = new HashSet<Interceptor>();

        for (TypeElement interceptorAnnotation : interceptorAnnotations) {

            Set<? extends Element> elementsAnnotatedWithInterceptor = roundEnv
                    .getElementsAnnotatedWith(interceptorAnnotation);
            Set<TypeElement> interceptorClassSet = ElementFilter.typesIn(elementsAnnotatedWithInterceptor);

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

                interceptors.add(new Interceptor(interceptorAnnotation, interceptorClassSet.iterator().next()));
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
                for (AnnotationMirror mirror : annotationMirrors) {

                    mLogger.trace("        Annotations on method: " + annotationMirrors);

                    for (Interceptor interceptor : interceptors) {

                        if (mTypeUtils.asElement(mirror.getAnnotationType()).equals(
                                interceptor.getInterceptorAnnotationElement())) {

                            mLogger.trace(String.format("        Added interceptor %s to method.", interceptor.getSimpleName()));
                            delegateMethod.addInterceptor(interceptor);
                        }
                    }
                }

                mLogger.trace("    Done processing method " + delegateMethod.getName());
            }
        }
    }
}
