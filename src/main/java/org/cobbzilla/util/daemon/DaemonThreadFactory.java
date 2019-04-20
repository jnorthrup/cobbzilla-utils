package org.cobbzilla.util.daemon;

import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

    public static final DaemonThreadFactory instance = new DaemonThreadFactory();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DaemonThreadFactory.class);

    @Override public Thread newThread(Runnable r) {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    public static ExecutorService fixedPool (int count) {
        if (count <= 0) {
            log.warn("fixedPool: invalid count ("+count+"), using single thread");
            count = 1;
        }
        return Executors.newFixedThreadPool(count, instance);
    }

}
