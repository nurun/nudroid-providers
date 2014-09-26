package com.nudroid.annotation.testbed;/*
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

import android.content.Context;
import android.database.Cursor;

import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.annotation.provider.delegate.PathParam;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.QueryParam;
import com.nudroid.provider.delegate.ContentProviderDelegate;

public class ParentClass {
    /**
     * A sample delegate to test the annotation processor.
     */
    @ContentProvider(authority = "com.nudroid.samples")
    public static class SampleContentProviderDelegate implements ContentProviderDelegate {

        @Query("/channels/test/{1}/{2}")
        public Cursor channels1(@PathParam("1") String variable1, @PathParam("2") int variable2,
                                @PathParam("3") String variable3) {

            //... perform query
            return null;
        }

        @Query("/channels/test/{1}/{2}")
        public Cursor channels2(@PathParam("1") String variable1, @PathParam("2") int variable2,
                                @PathParam("3") String variable3, @QueryParam("query") String queryParam) {

            return null;
        }

        @MyCacheInterceptor2.Interceptor
        @Query("/channels/test")
        public Cursor channels2(@QueryParam("test2") String test) {

            //... perform query
            return null;
        }
        //
        //        @Query("/channels/test")
        //        public Cursor channels3() {
        //
        //            //... perform query
        //            return null;
        //        }
        //
        //        @Query("/channels/test1")
        //        public Cursor channels4(@QueryParam("test1") String test) {
        //
        //            //... perform query
        //            return null;
        //        }
        //
        //        @MyCacheInterceptor2.Interceptor
        //        @Query("/channels/test1")
        //        public Cursor channels5(@QueryParam("test2") String test) {
        //
        //            //... perform query
        //            return null;
        //        }
        //
        //        @MyCacheInterceptor2.Interceptor
        //        @Query("/channels/test1")
        //        public Cursor channels6(@ContextRef Context context, @SelectionArgs String[] selectionArgs, @ContentUri Uri uri,
        //                                @Projection String[] projection, @Selection String selection,
        //                                @SortOrder String sortOrder) {
        //
        //            //... perform query
        //            return null;
        //        }

        @Override
        public boolean onCreate(Context context) {
            return false;
        }
    }
}

