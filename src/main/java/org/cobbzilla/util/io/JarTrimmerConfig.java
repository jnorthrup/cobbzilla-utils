package org.cobbzilla.util.io;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.collection.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.cobbzilla.util.string.StringUtil.UTF8cs;

@Accessors(chain=true)
public class JarTrimmerConfig {

    private File inJar;
    private File outJar;
    public File getOutJar () { return outJar != null ? outJar : inJar; }

    private String[] requiredClasses;
    @JsonIgnore
    private final Set<String> requiredClassSet = new HashSet<>(Arrays.asList(getRequiredClasses()));

    public JarTrimmerConfig setRequiredClassesFromFile (File f) throws IOException {
        requiredClasses = FileUtils.readLines(f, UTF8cs).toArray(new String[0]);
        return this;
    }

    private String[] requiredPrefixes = new String[] { "META-INF", "WEB-INF" };
    public JarTrimmerConfig requirePrefix(String prefix) { requiredPrefixes = ArrayUtil.append(requiredPrefixes, prefix); return this; }
    @JsonIgnore
    private final Set<String> requiredPrefixSet = new HashSet<>(Arrays.asList(getRequiredPrefixes()));

    private boolean includeRootFiles = true;
    private File counterFile = null;
    public boolean hasCounterFile () { return counterFile != null; }

    public boolean required(String name) {
        return getRequiredClassSet().contains(JarTrimmer.toClassName(name))
                || requiredByPrefix(name)
                || (includeRootFiles && !name.contains("/"));
    }

    private boolean requiredByPrefix(String name) {
        for (String prefix : getRequiredPrefixSet()) if (name.startsWith(prefix)) return true;
        return false;
    }

    public JarTrimmerConfig requireClasses(File file) throws IOException {
        requiredClasses = ArrayUtil.append(requiredClasses, FileUtils.readLines(file).toArray(new String[0]));
        return this;
    }

    public File getInJar() {
        return this.inJar;
    }

    public String[] getRequiredClasses() {
        return this.requiredClasses;
    }

    public Set<String> getRequiredClassSet() {
        return this.requiredClassSet;
    }

    public String[] getRequiredPrefixes() {
        return this.requiredPrefixes;
    }

    public Set<String> getRequiredPrefixSet() {
        return this.requiredPrefixSet;
    }

    public boolean isIncludeRootFiles() {
        return this.includeRootFiles;
    }

    public File getCounterFile() {
        return this.counterFile;
    }

    public JarTrimmerConfig setInJar(File inJar) {
        this.inJar = inJar;
        return this;
    }

    public JarTrimmerConfig setOutJar(File outJar) {
        this.outJar = outJar;
        return this;
    }

    public JarTrimmerConfig setRequiredClasses(String[] requiredClasses) {
        this.requiredClasses = requiredClasses;
        return this;
    }

    public JarTrimmerConfig setRequiredPrefixes(String[] requiredPrefixes) {
        this.requiredPrefixes = requiredPrefixes;
        return this;
    }

    public JarTrimmerConfig setIncludeRootFiles(boolean includeRootFiles) {
        this.includeRootFiles = includeRootFiles;
        return this;
    }

    public JarTrimmerConfig setCounterFile(File counterFile) {
        this.counterFile = counterFile;
        return this;
    }
}
