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
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.zip.DeflaterOutputStream;

import static com.github.hussainderry.crypto.Constants.*;


/**
 * Used to encrypt files using AES with PBKDF2
 * @author Hussain Al-Derry
 */
public class FileEncryptorAES {

    private final Cipher mAesCipher;
    private final SecureRandom mRandom;
    private final MessageDigest mDigest;
    private PBKDF2Helper mHelper;
    private ProgressMonitor mProgressMonitor;
    private byte[] key;
    private byte[] iv;
    private byte[] checksum;

    /**
     * Creates a new {@link FileEncryptorAES} instance with the following configurations:
     * AES Key Size: 128, PBKDF2 Salt Size: 32, PBKDF2 Iterations: 1000
     * @param password The base password to use
     * @return configured FileEncryptorAES instance
     * @throws IllegalStateException if there is an error initializing the cipher
     * @throws IllegalArgumentException if any of the arguments is null
     */
    public static FileEncryptorAES createEncryptorWithMinimumSecurityParams(String password){
        return new FileEncryptorAES(password, KeySize.KEY_128, Iterations.LOW, SaltSize.SALT_32);
    }

    /**
     * Creates a new {@link FileEncryptorAES} instance with the following configurations:
     * AES Key Size: 192, PBKDF2 Salt Size: 64, PBKDF2 Iterations: 10,000
     * @param password The base password to use
     * @return configured FileEncryptorAES instance
     * @throws IllegalStateException if there is an error initializing the cipher
     * @throws IllegalArgumentException if any of the arguments is null
     */
    public static FileEncryptorAES createEncryptorWithMediumSecurityParams(String password){
        return new FileEncryptorAES(password, KeySize.KEY_192, Iterations.MEDIUM, SaltSize.SALT_64);
    }

    /**
     * Creates a new {@link FileEncryptorAES} instance with the following configurations:
     * AES Key Size: 256, PBKDF2 Salt Size: 128, PBKDF2 Iterations: 20,000
     * @param password The base password to use
     * @return configured FileEncryptorAES instance
     * @throws IllegalStateException if there is an error initializing the cipher
     * @throws IllegalArgumentException if any of the arguments is null
     */
    public static FileEncryptorAES createEncryptorWithHighSecurityParams(String password){
        return new FileEncryptorAES(password, KeySize.KEY_256, Iterations.HIGH, SaltSize.SALT_128);
    }

    /**
     * Creates a new {@link FileEncryptorAES} instance custom configurations
     * @param password The base password to use
     * @param keySize The AES key size
     * @param pbkdf2SaltSize The size of salt used by PBKDF2
     * @param pbkdf2Iterations The PBKDF2 iterations
     * @return configured FileEncryptorAES instance
     * @throws IllegalStateException if there is an error initializing the cipher
     * @throws IllegalArgumentException if any of the arguments is null
     */
    public static FileEncryptorAES createEncryptorWithCustomSecurityParams(String password, KeySize keySize, Iterations pbkdf2Iterations, SaltSize pbkdf2SaltSize){
        return new FileEncryptorAES(password, keySize, pbkdf2Iterations, pbkdf2SaltSize);
    }

    private FileEncryptorAES(String password, KeySize keySize, Iterations pbkdf2Iterations, SaltSize pbkdf2SaltSize){
        if(password == null || keySize == null || pbkdf2Iterations == null || pbkdf2SaltSize == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        this.mHelper = new PBKDF2Helper.Builder(keySize)
                .saltSize(pbkdf2SaltSize)
                .iterations(pbkdf2Iterations)
                .build();
        this.mRandom = new SecureRandom();
        try {
            this.mAesCipher = Cipher.getInstance(CIPHER_PARAMS);
            this.mDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Unable to initialize cipher", e);
        }
        initParams(password);
    }

    /**
     * Set the {@link ProgressMonitor} to get updated about the status of the process
     * @param mProgressMonitor The {@link ProgressMonitor} instance to use
     * @throws IllegalArgumentException if any of the arguments is null
     */
    public void setProgressMonitor(ProgressMonitor mProgressMonitor) {
        if(mProgressMonitor == null){
            throw new IllegalArgumentException("ProgressMonitor cannot be null");
        }
        this.mProgressMonitor = mProgressMonitor;
    }

    /**
     * Initialize the encryption parameters
     * @param password The base password
     */
    private void initParams(String password){
        this.key = mHelper.createKeyFromPassword(password);
        this.checksum = mDigest.digest(password.getBytes(StandardCharsets.UTF_8));
        this.iv = new byte[IV_SIZE];
    }

    /**
     * Encrypts the source InputStream and writes it to the OutputStream
     * @param mInputStream {@link BufferedInputStream} The source to encrypt
     * @param mOutputStream {@link BufferedOutputStream} The target to write the encrypted data to
     * @return long The number of bytes encrypted
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws IllegalStateException if there is an IO exception
     */
    public long encrypt(BufferedInputStream mInputStream, BufferedOutputStream mOutputStream){
        if(mInputStream == null || mOutputStream == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        synchronized(mAesCipher){
            mRandom.nextBytes(iv);
            setModeEncrypt();

            try(DeflaterOutputStream mDeflaterOutputStream = new DeflaterOutputStream(mOutputStream);
                CipherOutputStream mAesOutputStream = new CipherOutputStream(mDeflaterOutputStream, mAesCipher)){

                mOutputStream.write(Base64.encodeBase64(checksum));
                mOutputStream.write(SEPARATOR);
                mOutputStream.write(Base64.encodeBase64(iv));
                mOutputStream.write(SEPARATOR);
                mOutputStream.write(Base64.encodeBase64(mHelper.getPbkdf2Configurations().getBytes(StandardCharsets.UTF_8)));
                mOutputStream.write(SEPARATOR);
                mOutputStream.flush();

                int itr = mInputStream.available() / BUFFER_SIZE;
                int counter = 0;

                byte[] buffer = new byte[BUFFER_SIZE];
                while(mInputStream.read(buffer) != -1){
                    mAesOutputStream.write(buffer);
                    publishProgress((counter++ * 100) / itr);
                }

                mAesOutputStream.flush();
                mAesOutputStream.close();
                mDeflaterOutputStream.finish();
                mDeflaterOutputStream.flush();
                mOutputStream.flush();

                return counter * BUFFER_SIZE;
            }catch(IOException e){
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * If there is a {@link ProgressMonitor} available, publish progress
     * @param progress The current progress
     */
    private void publishProgress(int progress){
        if(mProgressMonitor != null){
            mProgressMonitor.progressUpdated(progress);
        }
    }

    /**
     * Initializes the Cipher with the current encryption parameters
     */
    private void setModeEncrypt()  {
        try {
            mAesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new IvParameterSpec(iv));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Unable to init encryption mode", e);
        }
    }
}
