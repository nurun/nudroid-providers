package com.nudroid.provider.interceptor;

import com.nudroid.provider.delegate.ContentProviderContext;

import android.database.Cursor;
import android.net.Uri;

/**
 * A generic interceptor which can apply the same aspect to all types of delegate methods.
 * <p/>
 * Extending classes only need to implement the generic required abstract methods before and after.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class GenericContentProviderInterceptor implements ContentProviderInterceptor {

    /**
     * Called before target delegate method.
     * 
     * @param context
     *            A parameter object with all parameters that are to be passed to the delegate method.
     */
    public abstract void before(ContentProviderContext context);

    /**
     * Called after target delegate method.
     * 
     * @param result
     *            The result returned by the target delegate method.
     * @return The original result or a new result to replace the returned value.
     */
    public abstract <T> T after(ContentProviderContext contentProviderContext, T result);

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeQuery(com.nudroid.provider.delegate.ContentProviderContext)
     */
    @Override
    public void beforeQuery(ContentProviderContext contentProviderContext) {

        before(contentProviderContext);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterQuery(com.nudroid.provider.delegate.ContentProviderContext,
     *      android.database.Cursor)
     */
    @Override
    public Cursor afterQuery(ContentProviderContext contentProviderContext, Cursor result) {

        return after(contentProviderContext, result);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeUpdate(com.nudroid.provider.delegate.ContentProviderContext)
     */
    @Override
    public void beforeUpdate(ContentProviderContext contentProviderContext) {

        before(contentProviderContext);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterUpdate(com.nudroid.provider.delegate.ContentProviderContext,
     *      int)
     */
    @Override
    public int afterUpdate(ContentProviderContext contentProviderContext, int result) {

        return after(contentProviderContext, result);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeInsert(com.nudroid.provider.delegate.ContentProviderContext)
     */
    @Override
    public void beforeInsert(ContentProviderContext contentProviderContext) {

        before(contentProviderContext);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterInsert(com.nudroid.provider.delegate.ContentProviderContext,
     *      android.net.Uri)
     */
    @Override
    public Uri afterInsert(ContentProviderContext contentProviderContext, Uri result) {

        return after(contentProviderContext, result);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeDelete(com.nudroid.provider.delegate.ContentProviderContext)
     */
    @Override
    public void beforeDelete(ContentProviderContext contentProviderContext) {

        before(contentProviderContext);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterDelete(com.nudroid.provider.delegate.ContentProviderContext,
     *      int)
     */
    @Override
    public int afterDelete(ContentProviderContext contentProviderContext, int result) {

        return after(contentProviderContext, result);
    }
}
