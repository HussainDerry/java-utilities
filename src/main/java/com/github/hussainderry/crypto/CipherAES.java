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

import com.github.hussainderry.crypto.exception.DecryptionException;
import com.github.hussainderry.crypto.exception.EncryptionException;
import com.github.hussainderry.crypto.exception.KeyDerivationException;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static com.github.hussainderry.crypto.Constants.ALGORITHM;
import static com.github.hussainderry.crypto.Constants.CIPHER_PARAMS;
import static com.github.hussainderry.crypto.Constants.INT_SIZE;
import static com.github.hussainderry.crypto.Constants.IV_SIZE;
import static com.github.hussainderry.crypto.Constants.KEY_DERIVATION_ALGORITHM;
import static com.github.hussainderry.crypto.Constants.TAG_LENGTH;

/**
 * @author Hussain Al-Derry
 * @version 2.0
 * */
public final class CipherAES{

    private static final int KEY_SIZE = 256; // bits
    private static final int SALT_SIZE = 64; // bits
    private static final int PBKDF2_ITR = 600_000;

    private final SecureRandom mRandom;

    public CipherAES(){
        this.mRandom = new SecureRandom();
    }

    private Cipher createCipher(){
        try{
            return Cipher.getInstance(CIPHER_PARAMS);
        }catch(NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new EncryptionException("Unable to initialize cipher", e);
        }
    }

    private SecretKey generateSecretKey(char[] password, byte[] salt){
        try{
            KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITR, KEY_SIZE);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, ALGORITHM);
        }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new KeyDerivationException("Unable to create secret key", e);
        }
    }

    /**
     * encrypts the given data using AES-GCM-256
     * @param password The password to use for key derivation
     * @param data The data to be encrypted
     * @return The complete cipher text
     *
     * @throws IllegalArgumentException if the password or data are null
     * @throws EncryptionException if there is an error during the encryption process
     * */
    public byte[] encrypt(char[] password, byte[] data){
        if(password == null || password.length == 0){
            throw new IllegalArgumentException("Invalid Password");
        }else if(data == null || data.length == 0){
            throw new IllegalArgumentException("Invalid Data");
        }

        final char[] passwordCopy = Arrays.copyOf(password, password.length);
        final byte[] dataCopy = Arrays.copyOf(data, data.length);

        try{
            byte[] salt = new byte[SALT_SIZE];
            mRandom.nextBytes(salt);
            SecretKey secretKey = generateSecretKey(passwordCopy, salt);

            byte[] iv = new byte[IV_SIZE];
            mRandom.nextBytes(iv);
            GCMParameterSpec mSpec = new GCMParameterSpec(TAG_LENGTH, iv);

            Cipher cipher = createCipher();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, mSpec);
            byte[] ciphertext = cipher.doFinal(dataCopy);

            ByteBuffer mBuffer = ByteBuffer.allocate(INT_SIZE + SALT_SIZE + INT_SIZE + IV_SIZE + ciphertext.length);
            mBuffer.putInt(SALT_SIZE);
            mBuffer.put(salt);
            mBuffer.putInt(IV_SIZE);
            mBuffer.put(iv);
            mBuffer.put(ciphertext);

            return mBuffer.array();
        }catch(InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e){
            throw new EncryptionException("Unable to encrypt", e);
        }finally{
            Arrays.fill(passwordCopy, '\0');
            Arrays.fill(dataCopy, (byte) 0);
        }
    }

    /**
     * decrypts the given cipher text
     * @param password The password to use for key derivation
     * @param cipherMessage The cipher text to be decrypted
     * @return plain data
     *
     * @throws IllegalArgumentException if the password or data are null
     * @throws DecryptionException if there is an error during the decryption process
     * */
    public byte[] decrypt(char[] password, byte[] cipherMessage){
        if(password == null || password.length == 0){
            throw new IllegalArgumentException("Invalid Password");
        }else if(cipherMessage == null || cipherMessage.length == 0){
            throw new IllegalArgumentException("Invalid Cipher Data");
        }

        final char[] passwordCopy = Arrays.copyOf(password, password.length);

        try{
            ByteBuffer mBuffer = ByteBuffer.wrap(cipherMessage);
            // Getting salt
            int saltSize = mBuffer.getInt();
            if(saltSize != SALT_SIZE){
                throw new IllegalArgumentException("Invalid Salt Size: " + saltSize);
            }
            byte[] salt = new byte[SALT_SIZE];
            mBuffer.get(salt);

            // Creating secret key
            SecretKey secretKey = generateSecretKey(passwordCopy, salt);

            // Getting IV
            int ivSize = mBuffer.getInt();
            if(ivSize != IV_SIZE){
                throw new IllegalArgumentException("Invalid IV Size: " + ivSize);
            }
            byte[] iv = new byte[IV_SIZE];
            mBuffer.get(iv);

            // Creating param spec
            GCMParameterSpec mSpec = new GCMParameterSpec(TAG_LENGTH, iv);

            // Getting cipher text
            byte[] encryptedData = new byte[mBuffer.remaining()];
            mBuffer.get(encryptedData);

            Cipher cipher = createCipher();
            cipher.init(Cipher.DECRYPT_MODE, secretKey, mSpec);
            return cipher.doFinal(encryptedData);
        }catch(InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){
            throw new DecryptionException("Unable to decrypt", e);
        }finally{
            Arrays.fill(passwordCopy, '\0');
        }
    }
}
