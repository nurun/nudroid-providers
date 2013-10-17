package com.nudroid.annotation.processor;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

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
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        /*
         * Do not assume that because the @ContentProviderDelegate annotation can only be applied to types, only
         * TypeElements will be returned. Compilation errors on a class can let the compiler think the annotation is
         * applied to other elements even if it is correctly applied to a class, causing a class cast exception in the
         * for loop below.
         */
        Set<? extends Element> delegateClassTypes = roundEnv.getElementsAnnotatedWith(ContentProviderDelegate.class);

        mLogger.info(String.format("Start processing @%s annotations.", ContentProviderDelegate.class.getSimpleName()));
        mLogger.trace(String.format("    Classes annotated with @%s for the round: %s ",
                ContentProviderDelegate.class.getSimpleName(), delegateClassTypes));

        for (Element delegateClassType : delegateClassTypes) {

            if (delegateClassType instanceof TypeElement) {
                mLogger.trace("    Processing " + delegateClassType);
                processContentProviderDelegateAnnotation((TypeElement) delegateClassType, metadata);
                mLogger.trace("    Done processing " + delegateClassType);
            }
        }

        mLogger.info(String.format("Done processing @%s annotations.", ContentProviderDelegate.class.getSimpleName()));
    }

    private void processContentProviderDelegateAnnotation(TypeElement delegateClassType, Metadata metadata) {

        ContentProviderDelegate contentProviderDelegateAnnotation = delegateClassType
                .getAnnotation(ContentProviderDelegate.class);

        if (ElementUtils.isAbstract(delegateClassType)) {

            mLogger.trace("        Class is abstract. Signaling compilatoin error.");
            mLogger.error(
                    String.format("@%s annotations are only allowed on concrete classes",
                            ContentProviderDelegate.class.getSimpleName()), delegateClassType);
        }

        if (!validateClassIsTopLevelOrStatic(delegateClassType)) {

            mLogger.trace("        Class is not top level nor static. Signaling compilatoin error.");
            mLogger.error(String.format("@%s annotations can only appear on top level or static classes",
                    ContentProviderDelegate.class.getSimpleName()), delegateClassType);
        }

        if (!validateClassHasPublicDefaultConstructor(delegateClassType)) {

            mLogger.trace("        Class does not have a public default constructor. Signaling compilatoin error.");
            mLogger.error(String.format("Classes annotated with @%s must have a public default constructor",
                    ContentProviderDelegate.class.getSimpleName()), delegateClassType);
        }

        final String authorityName = contentProviderDelegateAnnotation.authority();
        mLogger.trace(String.format("        Authority name ='%s'.", authorityName));

        DelegateClass delegateClassForAuthority = metadata.getDelegateClassForAuthority(authorityName);

        if (delegateClassForAuthority != null) {

            mLogger.trace(String.format(
                    "        Authority is already registered by class %s. Signaling compilation error.",
                    delegateClassForAuthority));
            mLogger.error(String.format("Authority '%s' has already been registered by class %s", authorityName,
                    delegateClassForAuthority.getQualifiedName()), delegateClassType);
        }

        mLogger.trace(String.format("        Added delegate class %s to authority '%s'.", delegateClassType,
                authorityName));
        metadata.registerNewDelegateClass(authorityName, delegateClassType);
    }

    private boolean validateClassIsTopLevelOrStatic(TypeElement delegateClassType) {

        Set<Modifier> delegateClassModifiers = delegateClassType.getModifiers();

        Element enclosingDelegateClassElement = delegateClassType.getEnclosingElement();

        if (ElementUtils.isClassOrInterface(enclosingDelegateClassElement)
                && !delegateClassModifiers.contains(Modifier.STATIC)) {

            return false;
        }

        return true;
    }

    private boolean validateClassHasPublicDefaultConstructor(TypeElement delegateClassType) {

        List<? extends Element> enclosedElements = delegateClassType.getEnclosedElements();

        for (ExecutableElement constructor : ElementFilter.constructorsIn(enclosedElements)) {

            if (constructor.getParameters().size() == 0 && constructor.getModifiers().contains(Modifier.PUBLIC)) {

                return true;
            }
        }

        return false;
    }
}
