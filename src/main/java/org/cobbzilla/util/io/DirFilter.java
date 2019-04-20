package org.cobbzilla.util.io;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class DirFilter implements FileFilter {

    public static final DirFilter instance = new DirFilter();

    private String regex;

    private final Pattern pattern = initPattern();

    @java.beans.ConstructorProperties({"regex"})
    public DirFilter(String regex) {
        this.regex = regex;
    }

    public DirFilter() {
    }

    private Pattern initPattern() { return Pattern.compile(regex); }

    @Override public boolean accept(File pathname) {
        return pathname.isDirectory() && (empty(regex) || getPattern().matcher(pathname.getName()).matches());
    }

    public String getRegex() {
        return this.regex;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
