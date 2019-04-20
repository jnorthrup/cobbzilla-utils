package org.cobbzilla.util.io;

import java.io.File;
import java.io.FileFilter;

public class FileSuffixFilter implements FileFilter {

    private String suffix;

    @java.beans.ConstructorProperties({"suffix"})
    public FileSuffixFilter(String suffix) {
        this.suffix = suffix;
    }

    @Override public boolean accept(File pathname) { return pathname.getName().endsWith(suffix); }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
