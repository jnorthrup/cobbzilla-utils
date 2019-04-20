package org.cobbzilla.util.io.main;

import org.cobbzilla.util.collection.SingletonSet;
import org.cobbzilla.util.main.BaseMainOptions;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.Collection;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class FilesystemWatcherMainOptions extends BaseMainOptions {

    public static final String USAGE_COMMAND = "Command to run when something changes, must be executable. Default is to print the changes detected.";
    public static final String OPT_COMMAND = "-c";
    public static final String LONGOPT_COMMAND = "--command";
    @Option(name=OPT_COMMAND, aliases=LONGOPT_COMMAND, usage=USAGE_COMMAND, required=false)
    private String command = null;
    public boolean hasCommand () { return !empty(command); }

    public static final int DEFAULT_TIMEOUT = 600;
    public static final String USAGE_TIMEOUT = "Command will be run after this timeout (in seconds), regardless of any changes. Default is "+DEFAULT_TIMEOUT+" seconds ("+DEFAULT_TIMEOUT/60+" minutes).";
    public static final String OPT_TIMEOUT = "-t";
    public static final String LONGOPT_TIMEOUT = "--timeout";
    @Option(name=OPT_TIMEOUT, aliases=LONGOPT_TIMEOUT, usage=USAGE_TIMEOUT, required=false)
    private int timeout = DEFAULT_TIMEOUT;

    public static final int DEFAULT_MAXEVENTS = 100;
    public static final String USAGE_MAXEVENTS = "Command will be run after this many events have occurred. Default is " + DEFAULT_MAXEVENTS;
    public static final String OPT_MAXEVENTS = "-m";
    public static final String LONGOPT_MAXEVENTS = "--max-events";
    @Option(name=OPT_MAXEVENTS, aliases=LONGOPT_MAXEVENTS, usage=USAGE_MAXEVENTS, required=false)
    private int maxEvents = DEFAULT_MAXEVENTS;

    public static final String USAGE_DAMPER = "Command will never be run until there have been no events for this many seconds. Default is 0 (disabled). Takes precedence over "+OPT_TIMEOUT+"/"+LONGOPT_TIMEOUT;
    public static final String OPT_DAMPER = "-d";
    public static final String LONGOPT_DAMPER = "--damper";
    @Option(name=OPT_DAMPER, aliases=LONGOPT_DAMPER, usage=USAGE_DAMPER, required=false)
    private int damper = 0;
    public long getDamperMillis () { return damper * 1000; }

    public static final String USAGE_PATHS = "Paths to watch for changes. Default is the current directory";
    @Argument(usage=USAGE_PATHS)
    private List<String> paths = null;

    public boolean hasPaths () { return !empty(paths); }
    public Collection getWatchPaths() { return hasPaths() ? paths : new SingletonSet(System.getProperty("user.dir")); }

    public String getCommand() {
        return this.command;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public int getMaxEvents() {
        return this.maxEvents;
    }

    public int getDamper() {
        return this.damper;
    }

    public List<String> getPaths() {
        return this.paths;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public void setDamper(int damper) {
        this.damper = damper;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
