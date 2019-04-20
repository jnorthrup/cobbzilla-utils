package org.cobbzilla.util.daemon;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.concurrent.Future;

public class AwaitResult<T> {

    private Map<Future, T> successes = new HashMap<>();
    public void success(Future f, T thing) { successes.put(f, thing); }
    public int numSuccesses () { return successes.size(); }

    private Map<Future, Exception> failures = new HashMap<>();
    public void fail(Future f, Exception e) { failures.put(f, e); }
    public int numFails () { return failures.size(); }

    private List<Future> timeouts = new ArrayList<>();
    public void timeout (Collection<Future<?>> timedOut) { timeouts.addAll(timedOut); }
    public boolean timedOut() { return !timeouts.isEmpty(); }
    public int numTimeouts () { return timeouts.size(); }

    public boolean allSucceeded() { return failures.isEmpty() && timeouts.isEmpty(); }

    @JsonIgnore public List<T> getNotNullSuccesses() {
        final List<T> ok = new ArrayList<>();
        for (T t : getSuccesses().values()) if (t != null) ok.add(t);
        return ok;
    }

    public String toString() {
        return "successes=" + successes.size()
                + ", failures=" + failures.size()
                + ", timeouts=" + timeouts.size();
    }

    public Map<Future, T> getSuccesses() {
        return this.successes;
    }

    public Map<Future, Exception> getFailures() {
        return this.failures;
    }

    public List<Future> getTimeouts() {
        return this.timeouts;
    }
}
