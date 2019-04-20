package org.cobbzilla.util.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.now;

public class ReflectionUtilTest {

    @AllArgsConstructor
    public static class Dummy {
        @Getter @Setter public Long id;

        @Getter public String name;

        public void setName (String name) {
            this.name = name;
        }
        public void setName (Dummy something) {
            INSTANCE.die("should not get called!");
        }
    }

    private static final String ID = "id";
    public static final String NAME = "name";

    @Test public void testGetSet () throws Exception {

        Long testValue = INSTANCE.now();
        Dummy dummy = new Dummy(testValue, NAME);
        assertEquals(ReflectionUtil.INSTANCE.get(dummy, ID), testValue);

        ReflectionUtil.set(dummy, ID, null);
        assertNull(ReflectionUtil.INSTANCE.get(dummy, ID));

        testValue += 10;
        ReflectionUtil.set(dummy, ID, testValue);
        assertEquals(ReflectionUtil.INSTANCE.get(dummy, ID), testValue);

        ReflectionUtil.INSTANCE.setNull(dummy, ID, Long.class);
        assertNull(ReflectionUtil.INSTANCE.get(dummy, ID));

        ReflectionUtil.set(dummy, NAME, "a value");
        assertEquals(ReflectionUtil.INSTANCE.get(dummy, NAME), "a value");

        try {
            ReflectionUtil.set(dummy, NAME, null);
            fail("should not have been able to set name field to null");
        } catch (Exception expected) {}

        ReflectionUtil.INSTANCE.setNull(dummy, NAME, String.class);
        assertNull(ReflectionUtil.INSTANCE.get(dummy, NAME));


    }
}
