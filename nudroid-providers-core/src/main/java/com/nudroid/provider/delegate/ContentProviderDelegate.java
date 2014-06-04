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

package com.nudroid.provider.delegate;

import android.content.ContentProvider;
import android.content.Context;

/**
 * Optional interface which can be applied to a content provider delegate. If the content provider delegate implements
 * this interface, calls to {@link ContentProvider#onCreate()} will be forwarded to this interface onCreate() method.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface ContentProviderDelegate {

    /**
     * Implement this to initialize your delegate class. This method will be called by the
     * {@link ContentProvider#onCreate()} method and thus must follow the same semantics.
     * 
     * @param context
     *            The content provider context, as returned by {@link ContentProvider#getContext()}. There's no need to
     *            save this reference since the context can also be obtained by the annotation
     *            com.nudroid.annotation.provider.delegate.ContextRef.
     * 
     * @see ContentProvider#onCreate()
     * 
     * @return The value that is to be returned by the {@link ContentProvider#onCreate()} method to the Android
     *         platform.
     */
    public boolean onCreate(Context context);
}
