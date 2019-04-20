package org.cobbzilla.util.mustache;

import java.io.File;

class LAMFCacheKey {

    public File root;
    public String locale;

    @java.beans.ConstructorProperties({"root", "locale"})
    public LAMFCacheKey(File root, String locale) {
        this.root = root;
        this.locale = locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LAMFCacheKey)) return false;

        LAMFCacheKey that = (LAMFCacheKey) o;

        if (!root.equals(that.root)) return false;
        if (!locale.equals(that.locale)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = root.hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }
}
