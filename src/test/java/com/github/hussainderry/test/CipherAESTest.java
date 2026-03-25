package com.github.hussainderry.test;

import com.github.hussainderry.crypto.CipherAES;
import com.github.hussainderry.crypto.exception.DecryptionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class CipherAESTest {

    private CipherAES cipher;

    @Before
    public void init(){
        cipher = new CipherAES();
    }

    @Test
    public void plaintext_survives_encrypt_decrypt_round_trip(){
        char[] password = "test-password!@#".toCharArray();
        byte[] plaintext = "Hello, World! This is a secret message.".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = cipher.encrypt(password, plaintext);
        byte[] decrypted = cipher.decrypt(password, encrypted);

        Assert.assertArrayEquals(plaintext, decrypted);
    }

    @Test(expected = DecryptionException.class)
    public void decrypt_with_wrong_password_throws_decryption_exception(){
        char[] password = "correct-password".toCharArray();
        char[] wrongPassword = "wrong-password".toCharArray();
        byte[] plaintext = "secret data".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = cipher.encrypt(password, plaintext);
        cipher.decrypt(wrongPassword, encrypted);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encrypt_null_password_throws_illegal_argument(){
        cipher.encrypt(null, "data".getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void encrypt_null_data_throws_illegal_argument(){
        cipher.encrypt("password".toCharArray(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decrypt_null_password_throws_illegal_argument(){
        cipher.decrypt(null, "data".getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decrypt_null_data_throws_illegal_argument(){
        cipher.decrypt("password".toCharArray(), null);
    }

    @Test
    public void two_encryptions_of_same_data_produce_different_ciphertexts(){
        char[] password = "test-password".toCharArray();
        byte[] plaintext = "same data".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted1 = cipher.encrypt(password, plaintext);
        byte[] encrypted2 = cipher.encrypt(password, plaintext);

        Assert.assertFalse("Two encryptions of the same data should produce different ciphertexts",
                java.util.Arrays.equals(encrypted1, encrypted2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void encrypt_empty_password_throws_illegal_argument(){
        cipher.encrypt(new char[0], "data".getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void encrypt_empty_data_throws_illegal_argument(){
        cipher.encrypt("password".toCharArray(), new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decrypt_empty_password_throws_illegal_argument(){
        cipher.decrypt(new char[0], "data".getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decrypt_empty_data_throws_illegal_argument(){
        cipher.decrypt("password".toCharArray(), new byte[0]);
    }

}
