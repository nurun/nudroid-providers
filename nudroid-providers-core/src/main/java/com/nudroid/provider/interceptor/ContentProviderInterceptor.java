package com.nudroid.provider.interceptor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.nudroid.annotation.provider.delegate.Delete;
import com.nudroid.annotation.provider.delegate.Insert;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.Update;
import com.nudroid.provider.delegate.ContentProviderContext;

/**
 * A provider interceptor wraps invocations to content provider delegate methods and allows code to be executed before
 * and after the delegate method is invoked.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface ContentProviderInterceptor {

    /**
     * Called when the insterceptor is instantiated.
     * 
     * @param context
     *            The content provider context.
     */
    public void onCreate(Context context);

    /**
     * Called before {@link Query} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeQuery(ContentProviderContext contentProviderContext);

    /**
     * Called after {@link Query} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The cursor returned by the target method.
     * 
     * @return The original result or a new Cursor to replace the returned value.
     */
    public Cursor afterQuery(ContentProviderContext contentProviderContext, Cursor result);

    /**
     * Called before {@link Update} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeUpdate(ContentProviderContext contentProviderContext);

    /**
     * Called after {@link Update} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The integer returned by the target method.
     * 
     * @return The original result or a new integer to replace the returned value.
     */
    public int afterUpdate(ContentProviderContext contentProviderContext, int result);

    /**
     * Called before {@link Insert} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeInsert(ContentProviderContext contentProviderContext);

    /**
     * Called after {@link Insert} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The Uri returned by the target method.
     * 
     * @return The original result or a new Uri to replace the returned value.
     */
    public Uri afterInsert(ContentProviderContext contentProviderContext, Uri result);

    /**
     * Called before {@link Delete} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeDelete(ContentProviderContext contentProviderContext);

    /**
     * Called after {@link Delete} target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The integer returned by the target method.
     * 
     * @return The original result or a new integer to replace the returned value.
     */
    public int afterDelete(ContentProviderContext contentProviderContext, int result);
}
