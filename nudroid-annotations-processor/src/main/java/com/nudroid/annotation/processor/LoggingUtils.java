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

package com.nudroid.annotation.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * Utility class for logging messages while processing the annotations. To be compatible with IDEs, messages are logged
 * using the annotation processor's {@link Messager} object. This logging utility supports the standard logging levels,
 * TRACE, DEBUG, INFO, WARN and ERROR, which can be configured as it would be expected from any logging framework.
 * <p>
 * The TRACE, DEBUG and INFO levels are logged as a compiler {@link Kind#NOTE}. The WARN level is logged as a {@link
 * Kind#WARNING} and the ERROR level as a {@link Kind#ERROR}.
 * <p>
 * Only messages at a log level equal to or greater than the default log level will be issued.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class LoggingUtils {

    private static final String LOG_PATTERN = "%7s - %s";
    private final Messager messager;
    private final LogLevel logLevel;

    /**
     * Creates an instance of this class with a WARN default level.
     *
     * @param messager
     *         the {@link Messager} instance to use
     */
    LoggingUtils(Messager messager) {

        this(messager, LogLevel.WARN.toString());
    }

    /**
     * Creates an instance of this class.
     *
     * @param messager
     *         the {@link Messager} instance to use
     * @param defaultLogLevel
     *         the default log level: one of "TRACE", "DEBUG", "INFO", "WARN" or "ERROR"
     */
    LoggingUtils(Messager messager, String defaultLogLevel) {

        this.messager = messager;
        this.logLevel = LogLevel.valueOf(defaultLogLevel);
    }

    /**
     * Issues a log message at the TRACE level.
     *
     * @param message
     *         the message to be logged
     */
    public void trace(String message) {

        if (LogLevel.TRACE.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, String.format(LOG_PATTERN, LogLevel.TRACE, message));
        }
    }

    /**
     * Issues a log message at the DEBUG level.
     *
     * @param message
     *         the message to be logged
     */
    public void debug(String message) {

        if (LogLevel.DEBUG.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, String.format(LOG_PATTERN, LogLevel.DEBUG, message));
        }
    }

    /**
     * Issues a log message at the INFO level.
     *
     * @param message
     *         he message to be logged
     */
    public void info(String message) {

        if (LogLevel.INFO.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, String.format(LOG_PATTERN, LogLevel.INFO, message));
        }
    }

    /**
     * Issues a log message at the WARN level.
     *
     * @param message
     *         the message to be logged
     */
    public void warn(String message) {

        if (LogLevel.WARN.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, String.format(LOG_PATTERN, LogLevel.WARN, message));
        }
    }

    /**
     * Issues a log message at the WARN level at a particular {@link Element}. These log messages will be identified by
     * the compiler and modern IDEs and displayed besides the offending elements.
     *
     * @param message
     *         the message to be logged
     * @param element
     *         the {@link Element} this message is associated with. Can be null
     */
    public void warn(String message, Element element) {

        if (element != null) {

            messager.printMessage(Kind.WARNING, message, element);
        } else {

            messager.printMessage(Kind.WARNING, message);
        }
    }

    /**
     * Issues a log message at the ERROR level.
     *
     * @param message
     *         the message to be logged
     */
    public void error(String message) {

        if (LogLevel.ERROR.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, String.format(LOG_PATTERN, LogLevel.ERROR, message));
        }
    }

    /**
     * Issues a log message at the ERROR level at a particular {@link Element}. These log messages will be identified by
     * the compiler and modern IDEs and displayed besides the offending elements.
     *
     * @param message
     *         the message to be logged
     * @param element
     *         the {@link Element} this message is associated with. Can be null
     */
    public void error(String message, Element element) {

        if (element != null) {

            messager.printMessage(Kind.ERROR, message, element);
        } else {

            messager.printMessage(Kind.ERROR, message);
        }
    }

    /**
     * Generic log function.
     *
     * @param message
     *         the message to log
     * @param element
     *         the element to attach the message to
     * @param severity
     *         the severity of the message
     */
    public void log(String message, Element element, LogLevel severity) {

        switch (severity) {

            case TRACE:
                trace(message);
                break;
            case DEBUG:
                debug(message);
                break;
            case INFO:
                info(message);
                break;
            case WARN:
                warn(message, element);
                break;
            case ERROR:
                error(message, element);
                break;
        }
    }

    /**
     * The available severity levels.
     */
    public static enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR;

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return "[" + super.toString() + "]";
        }
    }
}
