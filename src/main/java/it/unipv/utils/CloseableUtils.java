package it.unipv.utils;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Classe utilizzata per chiudere possibili Closeable
 */
public class CloseableUtils {

    public static void close(Closeable... toClose) {
        for (Closeable c : toClose) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
    }
}
