/*
 * Copyright 2017 Hussain Al-Derry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hussainderry.crypto;

import com.github.hussainderry.crypto.enums.Iterations;
import com.github.hussainderry.crypto.enums.KeySize;
import com.github.hussainderry.crypto.enums.SaltSize;
import com.github.hussainderry.crypto.exception.EncryptionException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.github.hussainderry.crypto.Constants.ALGORITHM;
import static com.github.hussainderry.crypto.Constants.BUFFER_SIZE;
import static com.github.hussainderry.crypto.Constants.CIPHER_PARAMS;
import static com.github.hussainderry.crypto.Constants.INT_SIZE;
import static com.github.hussainderry.crypto.Constants.IV_SIZE;
import static com.github.hussainderry.crypto.Constants.TAG_LENGTH;


/**
 * Used to encrypt files using AES-GCM with PBKDF2 key derivation.
 * Instances are single-use: calling {@link #encrypt} more than once will throw.
 * @author Hussain Al-Derry
 */
public final class FileEncryptorAES {

    private final SecureRandom mRandom;
    private final PBKDF2Helper mHelper;
    private final byte[] key;
    private ProgressMonitor mProgressMonitor;
    private boolean used;

    /**
     * Creates a new {@link FileEncryptorAES} instance with minimum security params.
     * @param password The base password to use
     * @return configured FileEncryptorAES instance
     */
    public static FileEncryptorAES createEncryptorWithMinimumSecurityParams(char[] password){
        return new FileEncryptorAES(password, KeySize.KEY_128, Iterations.LOW, SaltSize.SALT_32);
    }

    /**
     * Creates a new {@link FileEncryptorAES} instance with medium security params.
     * @param password The base password to use
     * @return configured FileEncryptorAES instance
     */
    public static FileEncryptorAES createEncryptorWithMediumSecurityParams(char[] password){
        return new FileEncryptorAES(password, KeySize.KEY_192, Iterations.MEDIUM, SaltSize.SALT_64);
    }

    /**
     * Creates a new {@link FileEncryptorAES} instance with high security params.
     * @param password The base password to use
     * @return configured FileEncryptorAES instance
     */
    public static FileEncryptorAES createEncryptorWithHighSecurityParams(char[] password){
        return new FileEncryptorAES(password, KeySize.KEY_256, Iterations.HIGH, SaltSize.SALT_128);
    }

    /**
     * Creates a new {@link FileEncryptorAES} instance with custom configurations.
     * @param password The base password to use
     * @param keySize The AES key size
     * @param pbkdf2Iterations The PBKDF2 iterations
     * @param pbkdf2SaltSize The size of salt used by PBKDF2
     * @return configured FileEncryptorAES instance
     */
    public static FileEncryptorAES createEncryptorWithCustomSecurityParams(char[] password, KeySize keySize, Iterations pbkdf2Iterations, SaltSize pbkdf2SaltSize){
        return new FileEncryptorAES(password, keySize, pbkdf2Iterations, pbkdf2SaltSize);
    }

    private FileEncryptorAES(char[] password, KeySize keySize, Iterations pbkdf2Iterations, SaltSize pbkdf2SaltSize){
        if(password == null || keySize == null || pbkdf2Iterations == null || pbkdf2SaltSize == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        this.mHelper = new PBKDF2Helper.Builder(keySize)
                .saltSize(pbkdf2SaltSize)
                .iterations(pbkdf2Iterations)
                .build();
        this.mRandom = new SecureRandom();

        final char[] passwordCopy = Arrays.copyOf(password, password.length);
        try{
            this.key = mHelper.createKeyFromPassword(passwordCopy);
        }finally{
            Arrays.fill(passwordCopy, '\0');
        }
        this.used = false;
    }

    /**
     * Set the {@link ProgressMonitor} to get updated about the status of the process.
     * @param mProgressMonitor The {@link ProgressMonitor} instance to use
     */
    public void setProgressMonitor(ProgressMonitor mProgressMonitor){
        if(mProgressMonitor == null){
            throw new IllegalArgumentException("ProgressMonitor cannot be null");
        }
        this.mProgressMonitor = mProgressMonitor;
    }

    /**
     * Encrypts the source InputStream and writes it to the OutputStream.
     *
     * @param mInputStream {@link BufferedInputStream} The source to encrypt
     * @param mOutputStream {@link BufferedOutputStream} The target to write the encrypted data to
     * @return long The number of plaintext bytes read from the input
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws IllegalStateException if encrypt has already been called
     * @throws EncryptionException if an error occurs during encryption
     */
    public long encrypt(BufferedInputStream mInputStream, BufferedOutputStream mOutputStream){
        if(mInputStream == null || mOutputStream == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        if(used){
            throw new IllegalStateException("This encryptor has already been used. Create a new instance.");
        }
        used = true;

        byte[] fileIv = generateIv();
        writeHeader(mOutputStream, fileIv);
        writeSentinel(mOutputStream);
        return encryptData(mInputStream, mOutputStream, fileIv);
    }

    private byte[] generateIv(){
        byte[] iv = new byte[IV_SIZE];
        mRandom.nextBytes(iv);
        return iv;
    }

    private void writeHeader(BufferedOutputStream out, byte[] fileIv){
        byte[] config = mHelper.getPbkdf2Configurations().getBytes(StandardCharsets.UTF_8);
        ByteBuffer headerBuf = ByteBuffer.allocate((INT_SIZE * 2) + fileIv.length + config.length);
        headerBuf.putInt(fileIv.length);
        headerBuf.put(fileIv);
        headerBuf.putInt(config.length);
        headerBuf.put(config);
        try{
            out.write(headerBuf.array());
        }catch(IOException e){
            throw new EncryptionException("Failed to write header", e);
        }
    }

    private void writeSentinel(BufferedOutputStream out){
        byte[] sentinelIv = generateIv();
        byte[] sentinelCiphertext = encryptSentinel(sentinelIv);

        ByteBuffer sentinelBuf = ByteBuffer.allocate(IV_SIZE + INT_SIZE + sentinelCiphertext.length);
        sentinelBuf.put(sentinelIv);
        sentinelBuf.putInt(sentinelCiphertext.length);
        sentinelBuf.put(sentinelCiphertext);
        try{
            out.write(sentinelBuf.array());
            out.flush();
        }catch(IOException e){
            throw new EncryptionException("Failed to write sentinel", e);
        }
    }

    private byte[] encryptSentinel(byte[] sentinelIv){
        try{
            Cipher sentinelCipher = createCipher();
            initCipher(sentinelCipher, Cipher.ENCRYPT_MODE, sentinelIv);
            return sentinelCipher.doFinal(Constants.getSentinelPlaintext());
        }catch(IllegalBlockSizeException | BadPaddingException e){
            throw new EncryptionException("Unable to encrypt sentinel", e);
        }
    }

    private long encryptData(BufferedInputStream in, BufferedOutputStream out, byte[] fileIv){
        Cipher aesCipher = createCipher();
        initCipher(aesCipher, Cipher.ENCRYPT_MODE, fileIv);

        try(CipherOutputStream cipherOut = new CipherOutputStream(out, aesCipher)){
            long totalBytes = in.available();
            long bytesProcessed = 0;

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1){
                cipherOut.write(buffer, 0, bytesRead);
                bytesProcessed += bytesRead;
                if(totalBytes > 0){
                    publishProgress((int)((bytesProcessed * 100) / totalBytes));
                }
            }

            cipherOut.flush();
            cipherOut.close();
            out.flush();
            return bytesProcessed;
        }catch(IOException e){
            throw new EncryptionException("IO error during encryption", e);
        }
    }

    private void publishProgress(int progress){
        if(mProgressMonitor != null){
            mProgressMonitor.progressUpdated(progress);
        }
    }

    private Cipher createCipher(){
        try{
            return Cipher.getInstance(CIPHER_PARAMS);
        }catch(NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new EncryptionException("Unable to initialize cipher", e);
        }
    }

    private void initCipher(Cipher cipher, int mode, byte[] iv){
        try{
            cipher.init(mode, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(TAG_LENGTH, iv));
        }catch(InvalidKeyException | InvalidAlgorithmParameterException e){
            throw new EncryptionException("Unable to init cipher", e);
        }
    }
}
