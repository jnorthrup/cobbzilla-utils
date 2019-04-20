package org.cobbzilla.util.io

import java.io.File
import java.nio.file.Path
import java.nio.file.WatchEvent

abstract class CompositeBufferedFilesystemWatcher @java.beans.ConstructorProperties("timeout", "maxEvents")
constructor(val timeout: Long, val maxEvents: Int) : CompositeFilesystemWatcher<BufferedFilesystemWatcher>() {

    constructor(timeout: Long, maxEvents: Int, paths: Array<File>) : this(timeout, maxEvents) {
        addAll(paths)
    }

    constructor(timeout: Long, maxEvents: Int, paths: Array<String>) : this(timeout, maxEvents) {
        addAll(paths)
    }

    constructor(timeout: Long, maxEvents: Int, paths: Array<Path>) : this(timeout, maxEvents) {
        addAll(paths)
    }

    constructor(timeout: Long, maxEvents: Int, things: Collection<*>) : this(timeout, maxEvents) {
        addAll(things)
    }

    abstract fun fire(events: List<WatchEvent<*>>)
    private fun _fire(events: List<WatchEvent<*>>) {
        fire(events)
    }

    override fun newWatcher(path: Path): BufferedFilesystemWatcher {
        return object : BufferedFilesystemWatcher(path, timeout, maxEvents) {
            override fun fire(events: List<WatchEvent<*>>) {
                _fire(events)
            }
        }
    }

    override fun toString(): java.lang.String {
        return "CompositeBufferedFilesystemWatcher(super=" + super.toString() + ", timeout=" + this.timeout + ", maxEvents=" + this.maxEvents + ")"
    }
}
