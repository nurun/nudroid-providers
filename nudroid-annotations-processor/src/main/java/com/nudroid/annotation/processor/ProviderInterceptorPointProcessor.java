package com.nudroid.annotation.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

/**
 * Processes the Authority annotation.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class ProviderInterceptorPointProcessor {

    private Set<TypeElement> interceptorAnnotations = new HashSet<TypeElement>();
    private Metadata metadata;
    private Types typeUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    ProviderInterceptorPointProcessor(ProcessorContext processorContext) {

        this.metadata = processorContext.metadata;
        this.typeUtils = processorContext.typeUtils;
    }

    /**
     * Process the ProviderInterceptorPoint annotation on the given element.
     * 
     * @param roundEnv
     * 
     * @param executableElement
     *            The element to process.
     */
    void processInterceptorPoints(ExecutableElement methodElement) {

//        DelegateMethod method = metadata.getDelegateMethodForElement(methodElement);
//
//        List<? extends AnnotationMirror> annotationMirrors = methodElement.getAnnotationMirrors();
//
//        for (AnnotationMirror annotationMirror : annotationMirrors) {
//
//            if (interceptorAnnotations.contains(typeUtils.asElement(annotationMirror.getAnnotationType()))) {
//
//                if (method != null) {
//
//                    method.addInterceptors(continuation.getElementsAnnotatedWith(annotationMirror));
//                }
//            }
//        }
    }

    /**
     * Registers the set of elements as content provider interceptor annotations.
     * 
     * @param interceptorsToProcess
     *            The list of elements to process.
     */
    public void registerInterceptors(Set<TypeElement> interceptorsToProcess) {

        interceptorAnnotations.addAll(interceptorsToProcess);

        for (TypeElement element : interceptorAnnotations) {

//            metadata.addInterceptorClasses(continuation.getElementsAnnotatedWith(element));
        }
    }
}
