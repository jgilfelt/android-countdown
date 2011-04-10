/*
 * Copyright (C) 2011 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.countdown;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
//import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Chronometer;

public class CountdownChronometer extends Chronometer {
	private static final String TAG = "CountdownChronometer";

    private long mBase;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private boolean mLogged;
    private String mFormat;
    private Formatter mFormatter;
    private Locale mFormatterLocale;
    private Object[] mFormatterArgs = new Object[1];
    private StringBuilder mFormatBuilder;
    private OnChronometerTickListener mOnChronometerTickListener;
    private StringBuilder mRecycle = new StringBuilder(8);

    private static final int TICK_WHAT = 2;

    /**
     * Initialize this Chronometer object.
     * Sets the base to the current time.
     */
    public CountdownChronometer(Context context) {
        this(context, null, 0);
    }

    /**
     * Initialize with standard view layout information.
     * Sets the base to the current time.
     */
    public CountdownChronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initialize with standard view layout information and style.
     * Sets the base to the current time.
     */
    public CountdownChronometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    /**
     * Set the time that the count-up timer is in reference to.
     *
     * @param base Use the {@link SystemClock#elapsedRealtime} time base.
     */
//    @android.view.RemotableViewMethod
    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * Return the base time as set through {@link #setBase}.
     */
    public long getBase() {
        return mBase;
    }

    /**
     * Sets the format string used for display.  The Chronometer will display
     * this string, with the first "%s" replaced by the current timer value in
     * "MM:SS" or "H:MM:SS" form.
     *
     * If the format string is null, or if you never call setFormat(), the
     * Chronometer will simply display the timer value in "MM:SS" or "H:MM:SS"
     * form.
     *
     * @param format the format string.
     */
//    @android.view.RemotableViewMethod
    public void setFormat(String format) {
        mFormat = format;
        if (format != null && mFormatBuilder == null) {
            mFormatBuilder = new StringBuilder(format.length() * 2);
        }
    }

    /**
     * Returns the current format string as set through {@link #setFormat}.
     */
    public String getFormat() {
        return mFormat;
    }

    /**
     * Sets the listener to be called when the chronometer changes.
     *
     * @param listener The listener.
     */
    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    /**
     * @return The listener (may be null) that is listening for chronometer change
     *         events.
     */
    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    /**
     * Start counting up.  This does not affect the base as set from {@link #setBase}, just
     * the view display.
     *
     * Chronometer works by regularly scheduling messages to the handler, even when the
     * Widget is not visible.  To make sure resource leaks do not occur, the user should
     * make sure that each start() call has a reciprocal call to {@link #stop}.
     */
    public void start() {
        mStarted = true;
        updateRunning();
    }

    /**
     * Stop counting up.  This does not affect the base as set from {@link #setBase}, just
     * the view display.
     *
     * This stops the messages to the handler, effectively releasing resources that would
     * be held as the chronometer is running, via {@link #start}.
     */
    public void stop() {
        mStarted = false;
        updateRunning();
    }

    /**
     * The same as calling {@link #start} or {@link #stop}.
     * @hide pending API council approval
     */
//    @android.view.RemotableViewMethod
    public void setStarted(boolean started) {
        mStarted = started;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText(long now) {
        //long seconds = now - mBase;
    	long seconds = mBase - now;
        seconds /= 1000;
        String text = formatElapsedTime(mRecycle, seconds);

        if (mFormat != null) {
            Locale loc = Locale.getDefault();
            if (mFormatter == null || !loc.equals(mFormatterLocale)) {
                mFormatterLocale = loc;
                mFormatter = new Formatter(mFormatBuilder, loc);
            }
            mFormatBuilder.setLength(0);
            mFormatterArgs[0] = text;
            try {
                mFormatter.format(mFormat, mFormatterArgs);
                text = mFormatBuilder.toString();
            } catch (IllegalFormatException ex) {
                if (!mLogged) {
                    Log.w(TAG, "Illegal format string: " + mFormat);
                    mLogged = true;
                }
            }
        }
        setText(text);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1000);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }
	
    
    ////////
    
    private static final String FAST_FORMAT_HMMSS = "%1$d:%2$02d:%3$02d";
    private static final String FAST_FORMAT_MMSS = "%1$02d:%2$02d";
    private static final char TIME_PADDING = '0';
    private static final char TIME_SEPARATOR = ':';
    
    
    /**
     * Formats an elapsed time in the form "MM:SS" or "H:MM:SS"
     * for display on the call-in-progress screen.
     *
     * @param recycle {@link StringBuilder} to recycle, if possible
     * @param elapsedSeconds the elapsed time in seconds.
     */
    private static String formatElapsedTime(StringBuilder recycle, long elapsedSeconds) {
        //initFormatStrings();

        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        if (elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600;
            elapsedSeconds -= hours * 3600;
        }
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        seconds = elapsedSeconds;

        if (hours > 0) {
            return formatElapsedTime(recycle, FAST_FORMAT_HMMSS, hours, minutes, seconds);
        } else {
            return formatElapsedTime(recycle, FAST_FORMAT_MMSS, minutes, seconds);
        }
    }
    
    private static String formatElapsedTime(StringBuilder recycle, String format, long hours,
            long minutes, long seconds) {
        if (FAST_FORMAT_HMMSS.equals(format)) {
            StringBuilder sb = recycle;
            if (sb == null) {
                sb = new StringBuilder(8);
            } else {
                sb.setLength(0);
            }
            sb.append(hours);
            sb.append(TIME_SEPARATOR);
            if (minutes < 10) {
                sb.append(TIME_PADDING);
            } else {
                sb.append(toDigitChar(minutes / 10));
            }
            sb.append(toDigitChar(minutes % 10));
            sb.append(TIME_SEPARATOR);
            if (seconds < 10) {
                sb.append(TIME_PADDING);
            } else {
                sb.append(toDigitChar(seconds / 10));
            }
            sb.append(toDigitChar(seconds % 10));
            return sb.toString();
        } else {
            return String.format(format, hours, minutes, seconds);
        }
    }

    /**
     * Fast formatting of m:ss
     */
    private static String formatElapsedTime(StringBuilder recycle, String format, long minutes,
            long seconds) {
        if (FAST_FORMAT_MMSS.equals(format)) {
            StringBuilder sb = recycle;
            if (sb == null) {
                sb = new StringBuilder(8);
            } else {
                sb.setLength(0);
            }
            if (minutes < 10) {
                sb.append(TIME_PADDING);
            } else {
                sb.append(toDigitChar(minutes / 10));
            }
            sb.append(toDigitChar(minutes % 10));
            sb.append(TIME_SEPARATOR);
            if (seconds < 10) {
                sb.append(TIME_PADDING);
            } else {
                sb.append(toDigitChar(seconds / 10));
            }
            sb.append(toDigitChar(seconds % 10));
            return sb.toString();
        } else {
            return String.format(format, minutes, seconds);
        }
    }
    
    private static char toDigitChar(long digit) {
        return (char) (digit + '0');
    }
	
	
	
}
