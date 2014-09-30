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

package org.truth0.subjects;

import org.truth0.FailureStrategy;

/**
 * Hack to bypass possible compile-testing and Google Truth bugs.
 * <p>
 * This causes a few issues with error reporting since the failureStrategy can never be found n the reflected code.
 * <p>
 * Explanation:
 * By using compile-testing as documented on their site, the tests fails with an AbstractMethodError exception.
 * compile-testing uses Google Truth for assertions and, for some reason, it tries to invoke an abstract method on
 * the Subject class, event though the runtime instance in a child implementing the method. This could be due to
 * reflection.
 */
@Deprecated
public class Subject<S extends Subject<S, T>, T> extends com.google.common.truth.Subject<S, T> {

    public Subject(FailureStrategy failureStrategy, T subject) {
        super(failureStrategy, subject);
    }
}
