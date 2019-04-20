package org.cobbzilla.util.system;

import lombok.experimental.Accessors;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.ArrayUtils;
import org.cobbzilla.util.collection.SingletonList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.string.StringUtil.UTF8cs;

@Accessors(chain=true)
public class Command {

    public static final List<Integer> DEFAULT_EXIT_VALUES = new SingletonList<>(0);
    public static final int[] DEFAULT_EXIT_VALUES_INT = { 0 };

    private CommandLine commandLine;
    private String input;
    private byte[] rawInput;
    private InputStream stdin;
    private File dir;
    private Map<String, String> env;
    private List<Integer> exitValues = DEFAULT_EXIT_VALUES;

    private boolean copyToStandard = false;

    private OutputStream out;

    public Command() {
    }

    public boolean hasOut () { return out != null; }

    private OutputStream err;
    public boolean hasErr () { return err != null; }

    public Command(CommandLine commandLine) { this.commandLine = commandLine; }
    public Command(String command) { this(CommandLine.parse(command)); }
    public Command(File executable) { this(abs(executable)); }

    public boolean hasDir () { return dir != null; }
    public boolean hasInput () { return !empty(input) || !empty(rawInput) || stdin != null; }
    public InputStream getInputStream () {
        if (!hasInput()) return null;
        if (stdin != null) return stdin;
        if (rawInput != null) return new ByteArrayInputStream(rawInput);
        return new ByteArrayInputStream(input.getBytes(UTF8cs));
    }

    public int[] getExitValues () {
        return exitValues == DEFAULT_EXIT_VALUES
                ? DEFAULT_EXIT_VALUES_INT
                : ArrayUtils.toPrimitive(exitValues.toArray(new Integer[exitValues.size()]));
    }

    public Command setExitValues (List<Integer> values) { this.exitValues = values; return this; }

    public Command setExitValues (int[] values) {
        exitValues = new ArrayList<>(values.length);
        for (int v : values) exitValues.add(v);
        return this;
    }

    public CommandLine getCommandLine() {
        return this.commandLine;
    }

    public String getInput() {
        return this.input;
    }

    public byte[] getRawInput() {
        return this.rawInput;
    }

    public InputStream getStdin() {
        return this.stdin;
    }

    public File getDir() {
        return this.dir;
    }

    public Map<String, String> getEnv() {
        return this.env;
    }

    public boolean isCopyToStandard() {
        return this.copyToStandard;
    }

    public OutputStream getOut() {
        return this.out;
    }

    public OutputStream getErr() {
        return this.err;
    }

    public Command setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public Command setInput(String input) {
        this.input = input;
        return this;
    }

    public Command setRawInput(byte[] rawInput) {
        this.rawInput = rawInput;
        return this;
    }

    public Command setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    public Command setDir(File dir) {
        this.dir = dir;
        return this;
    }

    public Command setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public Command setCopyToStandard(boolean copyToStandard) {
        this.copyToStandard = copyToStandard;
        return this;
    }

    public Command setOut(OutputStream out) {
        this.out = out;
        return this;
    }

    public Command setErr(OutputStream err) {
        this.err = err;
        return this;
    }
}
