package com.nudroid.provider.interceptor.cache;

import android.text.format.Time;

/**
 * This class is not thread safe.
 * 
 * A {@link Clock} implementation which uses Android {@link Time} to get current date and time.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AndroidTimeClock implements Clock {
    Time mTime = new Time();

    @Override
    public long currentTime() {

        mTime.setToNow();
        return mTime.toMillis(false);
    }
}