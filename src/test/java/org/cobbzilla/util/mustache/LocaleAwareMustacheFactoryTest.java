package org.cobbzilla.util.mustache;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cobbzilla.util.io.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.cobbzilla.util.mustache.LocaleAwareMustacheFactory.TEMPLATE_SUFFIX;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LocaleAwareMustacheFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(LocaleAwareMustacheFactoryTest.class);

    public static final String LOCALE = "en_US";

    private File fileRoot;
    private LocaleAwareMustacheFactory factory;

    @Before
    public void setUp () throws Exception {
        fileRoot = FileUtil.INSTANCE.createTempDir(new File("/tmp"), "localeTest");
        factory = LocaleAwareMustacheFactory.Companion.getFactory(fileRoot, LOCALE);
    }

    @After
    public void tearDown () throws Exception {
        FileUtils.deleteDirectory(fileRoot);
    }

    @Test
    public void testFindOnlyFullResource () throws Exception {
        final String resource = "resource1.subject_en_US"+ Companion.getTEMPLATE_SUFFIX();
        populateTempDir(new String[] {resource});

        final List lines = IOUtils.readLines(factory.getReader("resource1.subject"));
        assertEquals("expected 1 line", 1, lines.size());
        assertEquals("wrong contents", resource, lines.get(0));
    }

    @Test
    public void testFindLanguageResource () throws Exception {
        final String resource = "resource1.subject"+ Companion.getTEMPLATE_SUFFIX();
        final String resource_en = "resource1.subject_en"+ Companion.getTEMPLATE_SUFFIX();
        populateTempDir(new String[] {resource, resource_en});

        final List lines = IOUtils.readLines(factory.getReader("resource1.subject"));
        assertEquals("expected 1 line", 1, lines.size());
        assertEquals("wrong contents", resource_en, lines.get(0));
    }

    @Test
    public void testFindDefaultResource () throws Exception {
        final String resource = "resource1.subject"+ Companion.getTEMPLATE_SUFFIX();
        populateTempDir(new String[] {resource});

        final List lines = IOUtils.readLines(factory.getReader("resource1.subject"));
        assertEquals("expected 1 line", 1, lines.size());
        assertEquals("wrong contents", resource, lines.get(0));
    }

    @Test
    public void testFindMostSpecificResource () throws Exception {
        final String resource = "resource1.subject"+ Companion.getTEMPLATE_SUFFIX();
        final String resource_en = "resource1.subject_en"+ Companion.getTEMPLATE_SUFFIX();
        final String resource_en_us = "resource1.subject_en_US"+ Companion.getTEMPLATE_SUFFIX();
        populateTempDir(new String[] {resource, resource_en, resource_en_us});

        final List lines = IOUtils.readLines(factory.getReader("resource1.subject"));
        assertEquals("expected 1 line", 1, lines.size());
        assertEquals("wrong contents", resource_en_us, lines.get(0));
    }

    @Test
    public void testResourceNotFound () throws Exception {
        try {
            factory.getReader("resource1.subject"+ Companion.getTEMPLATE_SUFFIX());
            fail("should have gotten resource not found");
        } catch (MustacheResourceNotFoundException e) {
            // expected
        }
    }

    private void populateTempDir(String[] files) throws IOException {
        for (String file : files) {
            FileUtils.writeStringToFile(new File(fileRoot, file), file);
        }
    }

}
