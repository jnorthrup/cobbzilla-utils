package org.cobbzilla.util.io;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.getDefaultTempDir;
import static org.cobbzilla.util.system.Sleep.sleep;

/**
 * A directory that implements Closeable. Use lombok @Cleanup to nuke it when it goes out of scope.
 */
public class TempDir extends File implements Closeable {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TempDir.class);

    private static class FileKillOrder implements Comparable<FileKillOrder> {
        private File file;
        private long killTime;

        @java.beans.ConstructorProperties({"file", "killTime"})
        public FileKillOrder(File file, long killTime) {
            this.file = file;
            this.killTime = killTime;
        }

        @Override public int compareTo(FileKillOrder k) {
            if (killTime > k.getKillTime()) return 1;
            if (killTime == k.getKillTime()) return 0;
            return -1;
        }
        public boolean shouldKill() { return now() > killTime; }

        public File getFile() {
            return this.file;
        }

        public long getKillTime() {
            return this.killTime;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public void setKillTime(long killTime) {
            this.killTime = killTime;
        }
    }

    private static class QuickTempReaper implements Runnable {
        private final SortedSet<FileKillOrder> temps = new ConcurrentSkipListSet<>();
        public File add (File t) { return add(t, now() + TimeUnit.MINUTES.toMillis(5)); }
        public File add (File t, long killTime) {
            synchronized (temps) {
                temps.add(new FileKillOrder(t, killTime));
                return t;
            }
        }
        @Override public void run() {
            while (true) {
                sleep(10_000);
                synchronized (temps) {
                    while (!temps.isEmpty() && temps.first().shouldKill()) {
                        if (!temps.first().getFile().delete()) {
                            log.warn("QuickTempReaper.run: couldn't delete " + abs(temps.first().getFile()));
                        }
                        temps.remove(temps.first());
                    }
                }
            }
        }
        public QuickTempReaper start () {
            daemon(this);
            return this;
        }
    }

    private static QuickTempReaper qtReaper = new QuickTempReaper().start();

    public static final long QT_NO_DELETE = -1L;

    public static File quickTemp() { return quickTemp(TimeUnit.MINUTES.toMillis(5)); }

    public static File quickTemp(final long killAfter) {
        try {
            if (killAfter > 0) {
                long killTime = killAfter + now();
                return qtReaper.add(File.createTempFile("quickTemp-", ".tmp", getDefaultTempDir()), killTime);
            } else {
                return File.createTempFile("quickTemp-", ".tmp", getDefaultTempDir());
            }
        } catch (IOException e) {
            return die("quickTemp: cannot create temp file: " + e, e);
        }
    }

    private interface TempDirOverrides { boolean delete(); }

    private File file;

    public TempDir () {
        super(abs(Files.createTempDir()));
        file = new File(super.getPath());
    }

    @Override public void close() throws IOException {
        if (!delete()) log.warn("close: error deleting TempDir: "+abs(file));
    }

    /**
     * Override to call 'delete', delete the entire directory.
     * @return true if the delete was successful.
     */
    @Override public boolean delete() { return FileUtils.deleteQuietly(file); }

}
