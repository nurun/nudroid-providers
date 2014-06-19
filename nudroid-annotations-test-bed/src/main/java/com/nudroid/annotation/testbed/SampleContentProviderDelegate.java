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

package com.nudroid.annotation.testbed;

import android.database.Cursor;

import com.nudroid.annotation.provider.delegate.*;

/**
 * A sample delegate to test the annotation processor.
 */
@ContentProvider(authority = "com.nudroid.samples")
public class SampleContentProviderDelegate {

    //    @Query("factions?name={name}")
    //    public Cursor findFactionByName(@UriPlaceholder("name") String name) {
    //
    //        return null;
    //    }
    //
    //    @Query("{type}/{id}")
    //    public Cursor listTypeById(@UriPlaceholder("type") String name, @UriPlaceholder("id") String id) {
    //
    //        return null;
    //    }
    //
    //    @Query("page/{type}/{id}")
    //    public Cursor listPage(@UriPlaceholder("type") String name, @UriPlaceholder("id") String id) {
    //
    //        return null;
    //    }
    //
    //
    //    @Query("{type}")
    //    public Cursor type(@UriPlaceholder("type") String name) {
    //
    //        return null;
    //    }

    @MyCacheInterceptor.Interceptor("{contentType}")
    @Query("{contentType}")
    public Cursor contentType(@UriPlaceholder("contentType") String contentType, @Selection String selection) {

        return null;
    }

//    @MyCacheInterceptor.Interceptor("{contentType}")
//    @Query("{contentType}?extra={extra}")
//    public Cursor contentTypeExtra(@UriPlaceholder("contentType") String name, @UriPlaceholder("extra") String extra) {
//
//        return null;
//    }
//
//    @MyCacheInterceptor.Interceptor("{contentType}")
//    @Query("{contentType}?com.nudroid.provider.interceptor.cache.CacheInterceptor.cacheId={cacheId}")
//    public Cursor contentTypeCache(@UriPlaceholder("contentType") String contentType,
//                                   @UriPlaceholder("cacheId") String cacheId) {
//
//        return null;
//    }
//
//    @MyCacheInterceptor.Interceptor("{contentType}")
//    @Query("{contentType}?extra={extra}&com.nudroid.provider.interceptor.cache.CacheInterceptor.cacheId={cacheId}")
//    public Cursor contentTypeExtraCache(@UriPlaceholder("contentType") String name,
//                                        @UriPlaceholder("extra") String extra,
//                                        @UriPlaceholder("cacheId") String cacheId) {
//
//        return null;
//    }
}
