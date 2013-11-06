package com.nudroid.annotation.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * Utility class for logging messages while processing the annotations. To be compatible with IDEs, messages are logged
 * using the annotation processor's {@link Messager} object. This logging utility supports the standard logging levels,
 * TRACE, DEBUG, INFO, WARN and ERROR, which can be configured as it would be expected from any logging framework. </p>
 * The TRACE, DEBUG and INFO levels are logged as a compiler {@link Kind#NOTE}. The WARN level is logged as a
 * {@link Kind#WARNING} and the ERROR level as a {@link Kind#ERROR}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class LoggingUtils {

    private static final String LOG_PATTERN = "%7s - %s";
    private Messager messager;
    private LogLevel logLevel;

    /**
     * Creates an instance of this class.
     * 
     * @param messager
     *            The {@link Messager} instance to use.
     * @param defaultLogLevel
     *            The default log level: one of "TRACE", "DEBUG", "INFO", "WARN" or "ERROR". Only messages at a log
     *            level equal to or greater than the default log level will be issued.
     */
    LoggingUtils(Messager messager, String defaultLogLevel) {

        this.messager = messager;

        if (defaultLogLevel == null) {
            this.logLevel = LogLevel.WARN;
        } else {
            this.logLevel = LogLevel.valueOf(defaultLogLevel);
        }
    }

    /**
     * Issues a log message at the TRACE level.
     * 
     * @param message
     *            The message to be logged.
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
     *            The message to be logged.
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
     *            The message to be logged.
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
     *            The message to be logged.
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
     *            The message to be logged.
     * @param element
     *            The {@link Element} this message is associated with. Can be null.
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
     *            The message to be logged.
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
     *            The message to be logged.
     * @param element
     *            The {@link Element} this message is associated with. Can be null.
     */
    public void error(String message, Element element) {

        if (element != null) {

            messager.printMessage(Kind.ERROR, message, element);
        } else {

            messager.printMessage(Kind.ERROR, message);
        }
    }

    private static enum LogLevel {
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
