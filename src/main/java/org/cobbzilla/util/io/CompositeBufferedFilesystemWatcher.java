package org.cobbzilla.util.io;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Collection;
import java.util.List;

public abstract class CompositeBufferedFilesystemWatcher extends CompositeFilesystemWatcher<BufferedFilesystemWatcher> {

    private final long timeout;
    private final int maxEvents;

    public CompositeBufferedFilesystemWatcher (long timeout, int maxEvents, File[] paths) {
        this(timeout, maxEvents);
        addAll(paths);
    }

    public CompositeBufferedFilesystemWatcher (long timeout, int maxEvents, String[] paths) {
        this(timeout, maxEvents);
        addAll(paths);
    }

    public CompositeBufferedFilesystemWatcher (long timeout, int maxEvents, Path[] paths) {
        this(timeout, maxEvents);
        addAll(paths);
    }

    public CompositeBufferedFilesystemWatcher (long timeout, int maxEvents, Collection things) {
        this(timeout, maxEvents);
        addAll(things);
    }

    @java.beans.ConstructorProperties({"timeout", "maxEvents"})
    public CompositeBufferedFilesystemWatcher(long timeout, int maxEvents) {
        this.timeout = timeout;
        this.maxEvents = maxEvents;
    }

    public abstract void fire(List<WatchEvent<?>> events);
    private void _fire(List<WatchEvent<?>> events) { fire(events); }

    @Override protected BufferedFilesystemWatcher newWatcher(Path path) {
        return new BufferedFilesystemWatcher(path, timeout, maxEvents) {
            @Override protected void fire(List<WatchEvent<?>> events) {
                _fire(events);
            }
        };
    }

    public java.lang.String toString() {
        return "CompositeBufferedFilesystemWatcher(super=" + super.toString() + ", timeout=" + this.timeout + ", maxEvents=" + this.maxEvents + ")";
    }

    public long getTimeout() {
        return this.timeout;
    }

    public int getMaxEvents() {
        return this.maxEvents;
    }
}
