package com.github.hussainderry.test;

import com.github.hussainderry.crypto.HashSHA;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class HashSHATest {

    @Test
    public void sha256_hash_produces_32_byte_output(){
        byte[] hash = HashSHA.hashUsingSHA256("test".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(32, hash.length);
    }

    @Test
    public void sha512_hash_produces_64_byte_output(){
        byte[] hash = HashSHA.hashUsingSHA512("test".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(64, hash.length);
    }

    @Test
    public void sha256_same_input_produces_identical_hash(){
        byte[] data = "consistent input".getBytes(StandardCharsets.UTF_8);
        byte[] hash1 = HashSHA.hashUsingSHA256(data);
        byte[] hash2 = HashSHA.hashUsingSHA256(data);
        Assert.assertArrayEquals(hash1, hash2);
    }

    @Test
    public void sha512_same_input_produces_identical_hash(){
        byte[] data = "consistent input".getBytes(StandardCharsets.UTF_8);
        byte[] hash1 = HashSHA.hashUsingSHA512(data);
        byte[] hash2 = HashSHA.hashUsingSHA512(data);
        Assert.assertArrayEquals(hash1, hash2);
    }

    @Test
    public void sha256_different_inputs_produce_different_hashes(){
        byte[] hash1 = HashSHA.hashUsingSHA256("input1".getBytes(StandardCharsets.UTF_8));
        byte[] hash2 = HashSHA.hashUsingSHA256("input2".getBytes(StandardCharsets.UTF_8));
        Assert.assertFalse(java.util.Arrays.equals(hash1, hash2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void sha256_null_input_throws_illegal_argument(){
        HashSHA.hashUsingSHA256(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sha512_null_input_throws_illegal_argument(){
        HashSHA.hashUsingSHA512(null);
    }

    @Test
    public void sha256_known_answer_matches_nist_digest(){
        byte[] hash = HashSHA.hashUsingSHA256("abc".getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for(byte b : hash){
            hex.append(String.format("%02x", b));
        }
        Assert.assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                hex.toString());
    }

    @Test
    public void sha256_empty_input_produces_known_digest(){
        byte[] hash = HashSHA.hashUsingSHA256(new byte[0]);
        StringBuilder hex = new StringBuilder();
        for(byte b : hash){
            hex.append(String.format("%02x", b));
        }
        Assert.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                hex.toString());
    }

}
