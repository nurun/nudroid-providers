package com.nudroid.provider.delegate;

import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.nudroid.annotation.provider.delegate.ContextRef;

/**
 * Optional interface which can be applied to a content provider delegate. If the content provider delegate implements
 * this interface, calls to {@link ContentProvider#onCreate()} will be delegated to classes implementing it.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface ContentProviderDelegate {

    /**
     * Implement this to initialize your content provider this delegate is responsible for on startup. This method will
     * be called by {@link ContentProvider#onCreate()} method and thus must follow the same semantics.
     * 
     * @param context
     *            The content provider context, as returned by {@link ContentProvider#getContext()}. There's no need to
     *            save this reference since the context can also be obtained by the annotation {@link ContextRef}.
     * 
     * @see ContentProvider#onCreate()
     * 
     * @return The value that is to be returned by the {@link ContentProvider#onCreate()} method to the Android
     *         platform.
     */
    public boolean onCreate(Context context);

    /**
     * Implement this delegate to return the {@link SQLiteOpenHelper} instance to be used by the content provider.
     * 
     * @param context
     *            The content provider's context.
     *            
     * @return The helper instance the content provider should use.
     */
    public SQLiteOpenHelper onCreateOpenHelper(Context context);
}
