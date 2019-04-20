package org.cobbzilla.util.io;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameSuffixFilter implements FilenameFilter {

    private String suffix;

    @java.beans.ConstructorProperties({"suffix"})
    public FilenameSuffixFilter(String suffix) {
        this.suffix = suffix;
    }

    @Override public boolean accept(File dir, String name) {
        return name.endsWith(suffix);
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
