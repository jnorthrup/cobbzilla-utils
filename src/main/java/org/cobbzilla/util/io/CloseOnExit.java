package org.cobbzilla.util.io;

import org.slf4j.Logger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class CloseOnExit implements Runnable {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CloseOnExit.class);
    private static List<Closeable> closeables = new ArrayList<>();

    private CloseOnExit () {}

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new CloseOnExit()));
    }

    @Override public void run() {
        if (closeables != null) {
            for (Closeable c : closeables) {
                try {
                    c.close();
                } catch (Exception e) {
                    log.error("Error closing: " + c + ": " + e, e);
                }
            }
        }
    }

    public static void add(Closeable closeable) { closeables.add(closeable); }

}
