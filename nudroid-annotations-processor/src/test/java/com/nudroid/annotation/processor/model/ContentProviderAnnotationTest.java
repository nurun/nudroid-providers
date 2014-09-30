/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.nudroid.annotation.processor.model;

import com.google.testing.compile.JavaFileObjects;
import com.nudroid.annotation.processor.Metadata;
import com.nudroid.annotation.processor.ProviderAnnotationProcessor;

import org.testng.annotations.Test;
import org.truth0.Truth;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;


public class ContentProviderAnnotationTest {

    @Test
    public void testRespectsPackageNaming() {

        JavaFileObject fileObject = JavaFileObjects.forResource("testee/RespectsPackageNamingTestSubject.java");

        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource(
                        "testee/generated_/RespectsPackageNamingTestSubjectContentProvider_.java"));
    }

    @Test
    public void testStripsContentProviderAndDelegateFromGeneratedClassName() {

        JavaFileObject fileObject = JavaFileObjects.forResource("testee/RedundantContentProviderDelegateSubject.java");
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(
                        JavaFileObjects.forResource("testee/generated_/RedundantSubjectContentProvider_.java"));
    }

    @Test
    public void testFailsIfDuplicateAuthority() {

        JavaFileObject fileObject = JavaFileObjects.forResource("testee/DuplicateAuthorityTestSubject.java");
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Authority 'DuplicateAuthorityTestSubject' has already been registered");
    }

    @Test
    public void testDefaultPackageNotAllowed() {

        JavaFileObject fileObject = JavaFileObjects.forResource("DefaultPackageNotAllowedTestSubject.java");
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Delegate classes cannot be defined in the default package");
    }

    @Test
    public void testAbstractClassesNotAllowed() {

        JavaFileObject fileObject = JavaFileObjects.forResource("testee/AbstractClassesNotAllowedTestSubject.java");
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("@ContentProvider annotations are only allowed on concrete classes");
    }

    @Test
    public void testInnerClassesMustBeStatic() {

        JavaFileObject fileObject = JavaFileObjects.forResource("testee/InnerClassesMustBeStaticTestSubject.java");
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("@ContentProvider annotations can only appear on top level or static classes");
    }

    @Test
    public void testClassMustHaveDefaultConstructor() {

        JavaFileObject fileObject =
                JavaFileObjects.forResource("testee/ClassMustHavePublicDefaultConstructorTestSubject.java");
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(new ProviderAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Classes annotated with @ContentProvider must have a public default constructor");
    }

    @Test
    public void testDetectsClassImplementsDelegateInterface() {

        JavaFileObject fileObject =
                JavaFileObjects.forResource("testee/DetectsClassImplementesDelegateInterfaceTestSubject.java");
        ProviderAnnotationProcessor processor = new ProviderAnnotationProcessor();
        Truth.ASSERT.about(javaSource())
                .that(fileObject)
                .processedWith(processor)
                .compilesWithoutError();

        Metadata metadata = processor.getMetadata();

        Truth.ASSERT.that(metadata.getDelegateClassForAuthority("DetectsClassImplementesDelegateInterfaceTestSubject")
                .getImplementsContentProviderDelegateInterface())
                .isTrue();
    }
}
