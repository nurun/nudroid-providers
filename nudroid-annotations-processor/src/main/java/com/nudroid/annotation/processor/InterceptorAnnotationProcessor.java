package com.nudroid.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.common.base.Joiner;
import com.nudroid.annotation.processor.model.AnnotationAttribute;
import com.nudroid.annotation.processor.model.ConcreteAnnotation;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateMethod;
import com.nudroid.annotation.processor.model.Interceptor;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint;
import com.nudroid.provider.interceptor.ContentProviderInterceptor;

/**
 * Add validation to interceptor constructors. <br/>
 * Processes @{@link ProviderInterceptorPoint} annotations.
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

        Set<TypeElement> interceptorAnnotations = continuation.getInterceptorAnnotationsForRound(roundEnv);

        mLogger.info(String.format("Start processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
        mLogger.trace(String.format("    Interfaces annotated with @%s for the round: %s",
                ProviderInterceptorPoint.class.getSimpleName(), interceptorAnnotations));
        
        for (TypeElement interceptorAnnotation : interceptorAnnotations) {

            continuation.popInterceptorAnnotation(interceptorAnnotation);
            createConcreteAnnotationMetadata(interceptorAnnotation, metadata);

            Element interceptorClass = interceptorAnnotation.getEnclosingElement();

            if (!ElementUtils.isClass(interceptorClass)) {

                mLogger.error(String.format("Interceptor annotations must be static elements of an eclosing %s",
                        ContentProviderInterceptor.class.getName()), interceptorAnnotation);
                continue;
            }

            if (!mTypeUtils.isAssignable(interceptorClass.asType(),
                    mElementUtils.getTypeElement(ContentProviderInterceptor.class.getName()).asType())) {

                mLogger.error(String.format("Interceptor class %s must implement interface %s", interceptorClass,
                        ContentProviderInterceptor.class.getName()), interceptorAnnotation);
                continue;
            }

            mLogger.trace(String.format("    Interceptor class for %s: %s", interceptorAnnotation, interceptorClass));

            processInterceptorAnnotation(metadata, (TypeElement) interceptorAnnotation, (TypeElement) interceptorClass);
        }

        mLogger.info(String.format("Done processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
    }

    private void createConcreteAnnotationMetadata(Element interceptorAnnotation, Metadata metadata) {

        if (interceptorAnnotation instanceof TypeElement) {

            ConcreteAnnotation annotation = new ConcreteAnnotation((TypeElement) interceptorAnnotation);

            final List<? extends Element> enclosedElements = new ArrayList<Element>(
                    interceptorAnnotation.getEnclosedElements());
            Collections.sort(enclosedElements, new Comparator<Element>() {

                @Override
                public int compare(Element o1, Element o2) {
                    return o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
                }
            });

            for (Element method : enclosedElements) {

                if (method instanceof ExecutableElement) {
                    annotation.addAttribute(new AnnotationAttribute((ExecutableElement) method));
                }
            }

            metadata.registerConcreteAnnotation(annotation);
        }
    }

    private void processInterceptorAnnotation(Metadata metadata, TypeElement interceptorAnnotation,
            TypeElement interceptorClass) {

        for (DelegateClass delagateClass : metadata.getDelegateClasses()) {

            for (DelegateMethod delegateMethod : delagateClass.getDelegateMethods()) {

                mLogger.trace("    Processing method " + delegateMethod.getName());
                ExecutableElement executableElement = delegateMethod.getExecutableElement();

                List<? extends AnnotationMirror> annotationsMirrors = executableElement.getAnnotationMirrors();

                for (AnnotationMirror mirror : annotationsMirrors) {

                    if (mTypeUtils.isSameType(mirror.getAnnotationType(), interceptorAnnotation.asType())) {

                        Interceptor interceptor = createInterceptorWithAnnotationLiterals(metadata,
                                interceptorAnnotation, interceptorClass, mirror);

                        delegateMethod.addInterceptor(interceptor);
                    }
                }

                mLogger.trace("    Done processing method " + delegateMethod.getName());
            }
        }
    }

    private Interceptor createInterceptorWithAnnotationLiterals(Metadata metadata, TypeElement interceptorAnnotation,
            TypeElement interceptorClass, AnnotationMirror mirror) {
        Interceptor interceptor = new Interceptor(interceptorAnnotation, interceptorClass, mTypeUtils,
                metadata.getConcreteAnnotation(interceptorAnnotation));

        SortedSet<ExecutableElement> methodKeys = new TreeSet<ExecutableElement>(new Comparator<ExecutableElement>() {

            @Override
            public int compare(ExecutableElement o1, ExecutableElement o2) {

                return o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
            }
        });

        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = mElementUtils
                .getElementValuesWithDefaults(mirror);
        methodKeys.addAll(annotationValues.keySet());

        for (ExecutableElement keyEntry : methodKeys) {

            ExecutableElement attribute = keyEntry;
            AnnotationValue attributeValue = annotationValues.get(keyEntry);

            generateAnnotationLiteral(interceptor, attribute, attributeValue);
        }

        return interceptor;
    }

    private void generateAnnotationLiteral(Interceptor interceptor, ExecutableElement attribute,
            AnnotationValue attributeValue) {
        final TypeKind kind = attribute.getReturnType().getKind();

        switch (kind) {
        case ARRAY:

            generateAnnotationArrayLiteral(interceptor, attribute, attributeValue);

            break;
        case CHAR:

            interceptor.addConcreteAnnotationConstructorLiteral(String.format("'%s'", attributeValue.getValue()));
            break;
        case FLOAT:

            interceptor.addConcreteAnnotationConstructorLiteral(String.format("%sf", attributeValue.getValue()));
            break;
        case LONG:

            interceptor.addConcreteAnnotationConstructorLiteral(String.format("%sL", attributeValue.getValue()));
            break;

        case DECLARED:

            TypeElement stringType = mElementUtils.getTypeElement(String.class.getName());

            if (mTypeUtils.isSameType(stringType.asType(), attribute.getReturnType())) {

                interceptor.addConcreteAnnotationConstructorLiteral(String.format("\"%s\"", attributeValue.getValue()));
            } else {

                final Element asElement = mTypeUtils.asElement(attribute.getReturnType());

                if (asElement.getKind() == ElementKind.ENUM) {
                    interceptor.addConcreteAnnotationConstructorLiteral(String.format("%s.%s", asElement,
                            attributeValue.getValue()));
                } else {

                    mLogger.error(String.format("Invalid type %s for the annotation attribute "
                            + "%s; only primitive type, String and enumeration are permitted or 1-dimensional arrays"
                            + " thereof.", asElement, attribute), attribute);
                }
            }

            break;

        default:
            interceptor.addConcreteAnnotationConstructorLiteral(String.format("%s", attributeValue.getValue()));
        }
    }

    private void generateAnnotationArrayLiteral(Interceptor interceptor, ExecutableElement attribute,
            AnnotationValue attributeValue) {
        ArrayType arrayType = (ArrayType) attribute.getReturnType();

        @SuppressWarnings("unchecked")
        List<? extends AnnotationValue> annotationValues = (List<? extends AnnotationValue>) attributeValue.getValue();
        List<Object> arrayElements = new ArrayList<Object>();

        for (AnnotationValue value : annotationValues) {

            arrayElements.add(value.getValue());
        }

        StringBuilder arrayInitializer = new StringBuilder();

        /*
         * On Eclipse, the toString() method of the AnnotationValues of an array is not proper. It does not print the
         * fully qualified name of enum, does not surround characters and strings wiith ' and " and does not append 'f'
         * or 'l' for floats or longs. Thus the need to recreate the logic for proper source code generation.
         */
        switch (arrayType.getComponentType().getKind()) {
        case CHAR:

            arrayInitializer.append("new char[] { '");
            Joiner.on("', '").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append("' }");
            break;
        case FLOAT:

            arrayInitializer.append("new float[] { ");
            Joiner.on("f, ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append("f }");
            break;
        case DOUBLE:

            arrayInitializer.append("new double[] { ");
            Joiner.on(", ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append(" }");
            break;
        case INT:

            arrayInitializer.append("new int[] { ");
            Joiner.on(", ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append(" }");
            break;
        case LONG:

            arrayInitializer.append("new long[] { ");
            Joiner.on("L, ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append("L }");
            break;

        case DECLARED:

            TypeElement stringType = mElementUtils.getTypeElement("java.lang.String");

            if (mTypeUtils.isSameType(stringType.asType(), arrayType.getComponentType())) {

                arrayInitializer.append("new String[] { \"");
                Joiner.on("\", \"").skipNulls().appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append("\" }");
            } else {

                final Element arrayElementType = mTypeUtils.asElement(arrayType.getComponentType());

                if (arrayElementType.getKind() == ElementKind.ENUM) {

                    arrayInitializer.append(String.format("new %s[] { %s.", arrayElementType, arrayElementType));
                    Joiner.on(String.format(", %s.", arrayElementType)).skipNulls()
                            .appendTo(arrayInitializer, arrayElements);
                    arrayInitializer.append(" }");
                } else {

                    mLogger.error(String.format("Invalid type %s for the annotation attribute "
                            + "%s; only primitive type, String and enumeration are permitted or 1-dimensional arrays"
                            + " thereof.", arrayElementType, attribute), attribute);
                }
            }

            break;
        default:
            break;
        }

        interceptor.addConcreteAnnotationConstructorLiteral(arrayInitializer.toString());
    }
}
