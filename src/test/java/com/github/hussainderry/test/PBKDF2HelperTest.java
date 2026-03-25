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
    public void generated_key_matches_requested_256_bit_length(){
        PBKDF2Helper mHelper = new PBKDF2Helper.Builder(KeySize.KEY_256).build();
        byte[] key = mHelper.createKeyFromPassword("test!@#".toCharArray());
        Assert.assertEquals(256, (key.length * 8));
    }

    @Test
    public void key_from_restored_config_matches_original_key(){
        PBKDF2Helper mHelper = new PBKDF2Helper.Builder(KeySize.KEY_256)
                .iterations(Iterations.MEDIUM)
                .saltSize(SaltSize.SALT_128)
                .build();

        byte[] firstKey = mHelper.createKeyFromPassword("test!@#".toCharArray());
        String config = mHelper.getPbkdf2Configurations();

        PBKDF2Helper mSecondHelper = new PBKDF2Helper.Builder(config).build();
        byte[] secondKey = mSecondHelper.createKeyFromPassword("test!@#".toCharArray());

        Assert.assertTrue(Arrays.equals(firstKey, secondKey));
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_password_throws_illegal_argument(){
        PBKDF2Helper helper = new PBKDF2Helper.Builder(KeySize.KEY_256).build();
        helper.createKeyFromPassword(null);
    }

    @Test
    public void generated_key_matches_requested_128_bit_length(){
        PBKDF2Helper helper = new PBKDF2Helper.Builder(KeySize.KEY_128).build();
        byte[] key = helper.createKeyFromPassword("test!@#".toCharArray());
        Assert.assertEquals(128, key.length * 8);
    }

    @Test
    public void generated_key_matches_requested_192_bit_length(){
        PBKDF2Helper helper = new PBKDF2Helper.Builder(KeySize.KEY_192).build();
        byte[] key = helper.createKeyFromPassword("test!@#".toCharArray());
        Assert.assertEquals(192, key.length * 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_null_key_size_throws_illegal_argument(){
        new PBKDF2Helper.Builder((KeySize) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_null_config_string_throws_illegal_argument(){
        new PBKDF2Helper.Builder((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_malformed_config_throws_illegal_argument(){
        new PBKDF2Helper.Builder("not:a:valid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_null_salt_size_throws_illegal_argument(){
        new PBKDF2Helper.Builder(KeySize.KEY_256).saltSize(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_null_iterations_throws_illegal_argument(){
        new PBKDF2Helper.Builder(KeySize.KEY_256).iterations(null);
    }

}
