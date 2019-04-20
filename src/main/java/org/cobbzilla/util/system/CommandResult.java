package org.cobbzilla.util.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.Accessors;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.string.StringUtil.UTF8;

@Accessors(chain=true)
public class CommandResult {

    // useful for mocks
    public static final CommandResult OK = new CommandResult(0, null, null);
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CommandResult.class);

    private String stdout;
    private String stderr;

    private Integer exitStatus;

    @JsonIgnore public boolean isZeroExitStatus () { return exitStatus != null && exitStatus == 0; }

    @JsonIgnore
    private Exception exception;
    public boolean hasException () { return exception != null; }
    public String getExceptionString () { return hasException() ? exception.toString() : null; }
    public void setExceptionString (String ex) { exception = new Exception(ex); }

    public CommandResult (Integer exitStatus, String stdout, String stderr) {
        this.exitStatus = (exitStatus == null) ? -1 : exitStatus;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public CommandResult (int exitValue, ByteArrayOutputStream out, ByteArrayOutputStream err) {
        this.exitStatus = exitValue;
        try {
            this.stdout = out == null ? null : out.toString(UTF8);
            this.stderr = err == null ? null : err.toString(UTF8);
        } catch (UnsupportedEncodingException e) {
            // should never happen
            die("CommandResult: couldn't convert stream to string: " + e, e);
        }
    }

    public CommandResult (Exception e) { this.exception = e; }

    public CommandResult (Exception e, ByteArrayOutputStream out, ByteArrayOutputStream err) {
        this.exception = e;
        try {
            this.stdout = out == null ? null : out.toString(UTF8);
            this.stderr = err == null ? null : err.toString(UTF8);
        } catch (UnsupportedEncodingException ex) {
            // should never happen
            log.warn("CommandResult: couldn't convert stream to string: " + ex, ex);
        }
    }

    @Override
    public String toString() {
        return "{" +
                "exitStatus=" + exitStatus +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", exception=" + getExceptionString() +
                '}';
    }

    public String getStdout() {
        return this.stdout;
    }

    public String getStderr() {
        return this.stderr;
    }

    public Integer getExitStatus() {
        return this.exitStatus;
    }

    public Exception getException() {
        return this.exception;
    }

    public CommandResult setStdout(String stdout) {
        this.stdout = stdout;
        return this;
    }

    public CommandResult setStderr(String stderr) {
        this.stderr = stderr;
        return this;
    }

    public CommandResult setExitStatus(Integer exitStatus) {
        this.exitStatus = exitStatus;
        return this;
    }
}
