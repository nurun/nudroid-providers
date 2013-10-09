package com.nudroid.annotation.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.nudroid.annotation.provider.delegate.Authority;

/**
 * Processes the Authority annotation.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class AuthorityProcessor {

    private Continuation continuation;
    private Metadata metadata;
    private ProcessingEnvironment processingEnv;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    AuthorityProcessor(ProcessorContext processorContext) {

        this.continuation = processorContext.continuation;
        this.metadata = processorContext.metadata;
        this.processingEnv = processorContext.processingEnv;
    }

    /**
     * Process the Authority annotation on the given class.
     * 
     * @param rootClass
     *            The class to process.
     */
    void processAuthorityOnClass(TypeElement rootClass) {

        Authority authority = rootClass.getAnnotation(Authority.class);

        if (authority != null) {

            continuation.addContinuationElement(rootClass);

            if (ElementUtils.isAbstract(rootClass)) {

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
}
