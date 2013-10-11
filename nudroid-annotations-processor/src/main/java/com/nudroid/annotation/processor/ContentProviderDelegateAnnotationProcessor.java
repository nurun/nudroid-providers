package com.nudroid.annotation.processor;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.provider.delegate.ContentProviderDelegate;

/**
 * Processes the {@link ContentProviderDelegate} annotation on a {@link TypeElement}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class ContentProviderDelegateAnnotationProcessor {

    private LoggingUtils mLogger;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The context for the provider annotation processor.
     */
    ContentProviderDelegateAnnotationProcessor(ProcessorContext processorContext) {

        this.mLogger = processorContext.logger;
    }

    /**
     * Process the {@link ContentProviderDelegate} annotations on this round.
     * 
     * @param roundEnv
     *            The round environment to process.
     * @param metadata
     *            The annotation metadata for the processor.
     */
    @SuppressWarnings("unchecked")
    void processContentProviderDelegateAnnotations(RoundEnvironment roundEnv, Metadata metadata) {

        Set<? extends TypeElement> delegateClassTypes = (Set<? extends TypeElement>) roundEnv
                .getElementsAnnotatedWith(ContentProviderDelegate.class);

        mLogger.info("Start processing ContentProviderDelegate annotations.");
        mLogger.trace("    Classes annotated with ContentProviderDelegate for the round " + delegateClassTypes);

        for (TypeElement delegateClassType : delegateClassTypes) {
            
            mLogger.trace("    Processing " + delegateClassType);
            processContentProviderDelegateAnnotation(delegateClassType, metadata);
            mLogger.trace("    Done processing " + delegateClassType);
        }

        mLogger.info("Done processing ContentProviderDelegate annotations.");
    }

    private void processContentProviderDelegateAnnotation(TypeElement delegateClassType, Metadata metadata) {

        ContentProviderDelegate contentProviderDelegateAnnotation = delegateClassType
                .getAnnotation(ContentProviderDelegate.class);

        if (contentProviderDelegateAnnotation != null) {

            if (ElementUtils.isAbstract(delegateClassType)) {

                mLogger.trace("        Class is abstract. Signaling compilatoin error.");
                mLogger.error("@ContentProviderDelegate annotations are only allowed on concrete classes",
                        delegateClassType);
            } else {

                final String authorityName = contentProviderDelegateAnnotation.authority();
                mLogger.trace(String.format("        Authority name ='%s'.", authorityName));
                
                DelegateClass delegateClassForAuthority = metadata.getDelegateClassForAuthority(authorityName);

                if (delegateClassForAuthority != null) {

                    mLogger.trace(String.format(
                            "        Authority is already registered by class %s. Signaling compilation error.",
                            delegateClassForAuthority));
                    mLogger.error(String.format("Authority '%s' has already been registered by class %s",
                            authorityName, delegateClassForAuthority.getName()), delegateClassType);
                }

                mLogger.trace(String.format("        Added delegate class %s to authority '%s'.", delegateClassType,
                        authorityName));
                metadata.registerAuthorityHandler(authorityName, delegateClassType);
            }
        }
    }
}
