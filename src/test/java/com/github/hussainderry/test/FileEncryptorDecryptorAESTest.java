package com.github.hussainderry.test;

import com.github.hussainderry.crypto.FileDecryptorAES;
import com.github.hussainderry.crypto.FileEncryptorAES;
import com.github.hussainderry.crypto.enums.Iterations;
import com.github.hussainderry.crypto.enums.KeySize;
import com.github.hussainderry.crypto.enums.SaltSize;
import com.github.hussainderry.crypto.exception.InvalidPasswordException;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class FileEncryptorDecryptorAESTest {

    private static final char[] PASSWORD = "test-password-!@#$".toCharArray();

    private FileEncryptorAES createEncryptor(){
        return FileEncryptorAES.createEncryptorWithCustomSecurityParams(
                PASSWORD, KeySize.KEY_128, Iterations.LOW, SaltSize.SALT_32);
    }

    private byte[] encrypt(byte[] original){
        FileEncryptorAES encryptor = createEncryptor();
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        try(BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(original));
            BufferedOutputStream out = new BufferedOutputStream(encryptedOut)){
            encryptor.encrypt(in, out);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return encryptedOut.toByteArray();
    }

    @Test
    public void file_data_survives_encrypt_decrypt_round_trip(){
        byte[] original = "This is test data for file encryption and decryption.".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(original);
        Assert.assertTrue("Encrypted data should not be empty", encrypted.length > 0);

        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        try(FileDecryptorAES decryptor = new FileDecryptorAES(PASSWORD);
            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(encrypted));
            BufferedOutputStream out = new BufferedOutputStream(decryptedOut)){
            decryptor.decrypt(in, out);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        Assert.assertArrayEquals(original, decryptedOut.toByteArray());
    }

    @Test
    public void wrong_password_throws_invalid_password_before_data_output(){
        byte[] original = "secret data".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(original);

        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        try(FileDecryptorAES decryptor = new FileDecryptorAES("wrong-password".toCharArray());
            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(encrypted));
            BufferedOutputStream out = new BufferedOutputStream(decryptedOut)){
            decryptor.decrypt(in, out);
            Assert.fail("Should have thrown on wrong password");
        }catch(InvalidPasswordException e){
            Assert.assertTrue("Error should mention invalid password",
                    e.getMessage().contains("Invalid password"));
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        Assert.assertEquals("No data should be written to output on wrong password",
                0, decryptedOut.toByteArray().length);
    }

    @Test(expected = IllegalStateException.class)
    public void second_encrypt_call_on_same_instance_throws_illegal_state(){
        FileEncryptorAES encryptor = createEncryptor();
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);

        try(BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data));
            BufferedOutputStream out = new BufferedOutputStream(new ByteArrayOutputStream())){
            encryptor.encrypt(in, out);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        try(BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data));
            BufferedOutputStream out = new BufferedOutputStream(new ByteArrayOutputStream())){
            encryptor.encrypt(in, out);
        }catch(IllegalStateException e){
            throw e;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void encrypt_with_null_input_stream_throws_illegal_argument(){
        createEncryptor().encrypt(null, new BufferedOutputStream(new ByteArrayOutputStream()));
    }

    @Test
    public void large_32kb_payload_survives_encrypt_decrypt_round_trip(){
        byte[] original = new byte[32768];
        for(int i = 0; i < original.length; i++){
            original[i] = (byte)(i % 256);
        }

        byte[] encrypted = encrypt(original);

        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        try(FileDecryptorAES decryptor = new FileDecryptorAES(PASSWORD);
            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(encrypted));
            BufferedOutputStream out = new BufferedOutputStream(decryptedOut)){
            decryptor.decrypt(in, out);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        Assert.assertArrayEquals(original, decryptedOut.toByteArray());
    }

    @Test
    public void empty_file_survives_encrypt_decrypt_round_trip(){
        byte[] original = new byte[0];
        byte[] encrypted = encrypt(original);
        Assert.assertTrue("Encrypted output should contain header even for empty file",
                encrypted.length > 0);

        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        try(FileDecryptorAES decryptor = new FileDecryptorAES(PASSWORD);
            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(encrypted));
            BufferedOutputStream out = new BufferedOutputStream(decryptedOut)){
            decryptor.decrypt(in, out);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        Assert.assertArrayEquals(original, decryptedOut.toByteArray());
    }

    @Test
    public void progress_monitor_is_called_during_encryption(){
        byte[] original = new byte[32768];
        for(int i = 0; i < original.length; i++){
            original[i] = (byte)(i % 256);
        }

        FileEncryptorAES encryptor = createEncryptor();
        final boolean[] progressCalled = {false};
        encryptor.setProgressMonitor(progress -> progressCalled[0] = true);

        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        try(BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(original));
            BufferedOutputStream out = new BufferedOutputStream(encryptedOut)){
            encryptor.encrypt(in, out);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        Assert.assertTrue("ProgressMonitor should have been called at least once", progressCalled[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decryptor_null_password_throws_illegal_argument(){
        new FileDecryptorAES(null);
    }

    @Test
    public void decryptor_reuse_decrypts_two_different_files(){
        byte[] data1 = "First file content".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Second file content entirely different".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted1 = encrypt(data1);
        byte[] encrypted2 = encrypt(data2);

        try(FileDecryptorAES decryptor = new FileDecryptorAES(PASSWORD)){
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            try(BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(encrypted1));
                BufferedOutputStream out = new BufferedOutputStream(out1)){
                decryptor.decrypt(in, out);
            }
            Assert.assertArrayEquals(data1, out1.toByteArray());

            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            try(BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(encrypted2));
                BufferedOutputStream out = new BufferedOutputStream(out2)){
                decryptor.decrypt(in, out);
            }
            Assert.assertArrayEquals(data2, out2.toByteArray());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}
