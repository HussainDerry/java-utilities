package com.github.hussainderry.test;

import com.github.hussainderry.crypto.PBKDF2Helper;
import com.github.hussainderry.crypto.enums.Iterations;
import com.github.hussainderry.crypto.enums.KeySize;
import com.github.hussainderry.crypto.enums.SaltSize;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class PBKDF2HelperTest {

    @Test
    public void testKeySize(){
        PBKDF2Helper mHelper = new PBKDF2Helper.Builder(KeySize.KEY_256).build();
        byte[] key = mHelper.createKeyFromPassword("test!@#");
        Assert.assertEquals(256, (key.length * 8));
    }

    @Test
    public void testConfigurations(){
        PBKDF2Helper mHelper = new PBKDF2Helper.Builder(KeySize.KEY_256)
                .iterations(Iterations.MEDIUM)
                .saltSize(SaltSize.SALT_128)
                .build();

        byte[] firstKey = mHelper.createKeyFromPassword("test!@#");
        String config = mHelper.getPbkdf2Configurations();

        PBKDF2Helper mSecondHelper = new PBKDF2Helper.Builder(config).build();
        byte[] secondKey = mSecondHelper.createKeyFromPassword("test!@#");

        Assert.assertTrue(Arrays.equals(firstKey, secondKey));
    }

}
