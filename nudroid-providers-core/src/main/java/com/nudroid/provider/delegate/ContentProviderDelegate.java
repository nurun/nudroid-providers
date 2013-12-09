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
