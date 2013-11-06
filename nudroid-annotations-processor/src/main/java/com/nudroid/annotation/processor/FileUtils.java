package com.nudroid.annotation.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

/**
 * Utility classes for managing files and streams.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class FileUtils {

    /**
     * Closes the specified scanner.
     * 
     * @param scanner
     *            The scanner to close.
     */
    static void close(Scanner scanner) {

        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Closes the specified reader.
     * 
     * @param reader
     *            The reader to close.
     * @throws RuntimeException
     *             if there's an error while closing the reader.
     */
    static void close(Reader reader) {

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the specified writer.
     * 
     * @param writer
     *            The writer to close.
     * @throws RuntimeException
     *             if there's an error while closing the writer.
     */
    static void close(Writer writer) {

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
