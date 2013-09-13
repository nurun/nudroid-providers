package com.nudroid.persistence.annotation.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class LoggingUtils {

    private Messager messager;
    private LogLevel logLevel;

    LoggingUtils(Messager messager, String defaultLogLevel) {

        this.messager = messager;

        if (defaultLogLevel == null) {
            this.logLevel = LogLevel.WARN;
        } else {
            this.logLevel = LogLevel.valueOf(defaultLogLevel);
        }
    }

    void trace(String message) {

        if (LogLevel.TRACE.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, "[TRACE] " + message);
        }
    }

    void debug(String message) {

        if (LogLevel.DEBUG.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, "[DEBUG] " + message);
        }
    }

    void info(String message) {

        if (LogLevel.INFO.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, "[INFO] " + message);
        }
    }

    void warn(String message) {

        if (LogLevel.WARN.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, "[WARN] " + message);
        }
    }

    void warn(String message, Element element) {

        messager.printMessage(Kind.WARNING, message, element);
    }

    void error(String message) {

        if (LogLevel.ERROR.ordinal() >= logLevel.ordinal()) {

            messager.printMessage(Kind.NOTE, "[ERROR] " + message);
        }
    }

    void error(String message, Element element) {

        messager.printMessage(Kind.ERROR, message, element);
    }

    private static enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
