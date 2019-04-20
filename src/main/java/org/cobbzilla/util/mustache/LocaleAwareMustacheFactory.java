package org.cobbzilla.util.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static org.cobbzilla.util.daemon.ZillaRuntime.now;
import static org.cobbzilla.util.string.StringUtil.DEFAULT_LOCALE;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
@Slf4j
public class LocaleAwareMustacheFactory extends DefaultMustacheFactory {

    public static final String TEMPLATE_SUFFIX = ".mustache";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LocaleAwareMustacheFactory.class);

    @Getter @Setter private static boolean skipClasspath = false;

    private final File superFileRoot; // superclass should have this protected, not private
    private final List<String> suffixChecks;

    protected static final LoadingCache<LAMFCacheKey, LocaleAwareMustacheFactory> factoryLoadingCache
            = CacheBuilder.newBuilder().build(new CacheLoader<LAMFCacheKey, LocaleAwareMustacheFactory>() {
        @Override
        public LocaleAwareMustacheFactory load(LAMFCacheKey key) throws Exception {
            return new LocaleAwareMustacheFactory(key.root, key.locale);
        }
    });

    public static LocaleAwareMustacheFactory getFactory (File fileRoot, Locale locale) throws ExecutionException {
        return getFactory(fileRoot, (locale == null) ? DEFAULT_LOCALE : locale.toString());
    }

    private static final AtomicLong lastRefresh = new AtomicLong(now());
    private static final long REFRESH_INTERVAL = 1000 * 60 * 5; // 5 minutes

    public synchronized static LocaleAwareMustacheFactory getFactory(File fileRoot, String locale) throws ExecutionException {
        if (locale == null) locale = DEFAULT_LOCALE;
        if (now() > lastRefresh.longValue() + REFRESH_INTERVAL) {
            flushCache();
        }
        return factoryLoadingCache.get(new LAMFCacheKey(fileRoot, locale));
    }

    public static void flushCache() {
        factoryLoadingCache.invalidateAll();
        lastRefresh.set(now());
    }

    public LocaleAwareMustacheFactory (File fileRoot, Locale locale) {
        this(fileRoot, locale.toString());
    }

    public LocaleAwareMustacheFactory (File fileRoot, String locale) {
        super(fileRoot);
        this.superFileRoot = fileRoot;
        this.suffixChecks = new ArrayList<>(4);
        StringBuilder suffix = new StringBuilder();
        for (String localePart : locale.split("_")) {
            if (suffix.length() > 0) suffix.append('_');
            suffix.append(localePart);
            suffixChecks.add(0, suffix.toString());
        }
        suffixChecks.add(""); // default template
    }

    public static boolean isSkipClasspath() {
        return LocaleAwareMustacheFactory.skipClasspath;
    }

    public static void setSkipClasspath(boolean skipClasspath) {
        LocaleAwareMustacheFactory.skipClasspath = skipClasspath;
    }

    private class LAMFCacheLoader extends CacheLoader<String, Mustache> {
        @Override
        public Mustache load(String key) throws Exception {
//            return mc.compile(abs(superFileRoot)+"/"+key);
            return mc.compile(key);
        }
    }

    @Override
    protected LoadingCache<String, Mustache> createMustacheCache() {
        return CacheBuilder.newBuilder().build(new LAMFCacheLoader());
    }

    @Override
    public Reader getReader(String resourceName) {
        for (String suffix : suffixChecks) {
            // don't add a trailing _ if there is not suffix, since the default resource is
            // simply "path/to/resourceName" not "path/to/resourceName_"
            final String name = (suffix.length() == 0) ? resourceName + TEMPLATE_SUFFIX : resourceName + "_" + suffix + TEMPLATE_SUFFIX;
            try {
                return getReader_internal(name);
            } catch (MustacheException e) {
                log.debug("getReader: didn't find resource at "+name);
            }
        }
        throw new MustacheResourceNotFoundException("getReader: no resource (not even a default resource) found at all: "+resourceName);
    }

    private Reader getReader_internal(String name) {
        if (skipClasspath) {
            File file = superFileRoot == null ? new File(name) : new File(superFileRoot, name);
            if (file.exists() && file.isFile()) {
                try {
                    InputStream in = new FileInputStream(file);
                    return new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
                } catch (IOException e) {
                    throw new MustacheException("could not open: " + file, e);
                }
            } else {
                throw new MustacheException("not a file: " + file);
            }
        } else {
            return super.getReader(name);
        }
    }

    public String render(String templateName, Map<String, Object> scope) {
        final StringWriter writer = new StringWriter();
        if (!render(templateName, scope, writer)) return null;
        return writer.getBuffer().toString().trim();
    }

    public boolean render(String templateName, Map<String, Object> scope, Writer writer) {
        try {
            compile(templateName).execute(writer, scope);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof MustacheResourceNotFoundException) {
                return false;
            } else {
                throw e;
            }
        } catch (MustacheResourceNotFoundException e) {
            return false;
        }
        return true;
    }

}
