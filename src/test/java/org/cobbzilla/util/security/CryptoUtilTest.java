package org.cobbzilla.util.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CryptoUtilTest {

    @Test public void testStringEncryption () throws Exception {
        final String plaintext = RandomStringUtils.randomAlphanumeric(50+RandomUtils.nextInt(100, 500));
        final String key = RandomStringUtils.randomAlphanumeric(20);
        final String ciphertext = CryptoUtil.INSTANCE.string_encrypt(plaintext, key);
        assertEquals(plaintext, CryptoUtil.INSTANCE.string_decrypt(ciphertext, key));
    }

}
