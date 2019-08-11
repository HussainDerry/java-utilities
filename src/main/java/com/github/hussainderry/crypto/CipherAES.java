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

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

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
 * @version 1.0
 * */
public final class CipherAES{

    private static final int KEY_SIZE = 256; // bits
    private static final int SALT_SIZE = 64; // bits
    private static final int PBKDF2_ITR = 65_536;

    private final SecureRandom mRandom;
    private final Cipher mCipher;

    public CipherAES(){
        try{
            mCipher = Cipher.getInstance(CIPHER_PARAMS);
        }catch(NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new IllegalStateException("Unable To Initialize Cipher: " + e.getMessage());
        }
        this.mRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes());
    }

    private SecretKey generateSecretKey(char[] password, byte [] iv){
        try{
            KeySpec spec = new PBEKeySpec(password, iv, PBKDF2_ITR, KEY_SIZE); // AES-256
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, ALGORITHM);
        }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new IllegalStateException("Unable To Create Secret Key: " + e.getMessage());
        }
    }

    /**
     * encrypts the given data using AES-GCM-256
     * @param password The password to use for key derivation
     * @param data The data to be encrypted
     * @return The complete cipher text
     *
     * @throws IllegalArgumentException if the password or data are null
     * @throws IllegalStateException if there is an error during the encryption process
     * */
    public byte[] encrypt(char[] password, byte[] data){
        if(password == null || password.length == 0){
            throw new IllegalArgumentException("Invalid Password");
        }else if(data == null || data.length == 0){
            throw new IllegalArgumentException("Invalid Data");
        }

        try{
            byte[] salt = new byte[SALT_SIZE];
            mRandom.nextBytes(salt);
            SecretKey mKey = generateSecretKey(password, salt);

            byte[] iv = new byte[IV_SIZE];
            mRandom.nextBytes(iv);
            GCMParameterSpec mSpec = new GCMParameterSpec(TAG_LENGTH, iv);

            byte[] res = null;
            synchronized(mCipher){
                mCipher.init(Cipher.ENCRYPT_MODE, mKey, mSpec);
                res = mCipher.doFinal(data);
            }

            ByteBuffer mBuffer = ByteBuffer.allocate(INT_SIZE + SALT_SIZE + INT_SIZE + IV_SIZE + res.length);
            mBuffer.putInt(SALT_SIZE);
            mBuffer.put(salt);
            mBuffer.putInt(IV_SIZE);
            mBuffer.put(iv);
            mBuffer.put(res);

            return mBuffer.array();
        }catch(InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e){
            throw new IllegalStateException("Unable To Encrypt: " + e.getMessage());
        }
    }

    /**
     * decrypts the given cipher text
     * @param password The password to use for key derivation
     * @param cipherMessage The cipher text to be decrypted
     * @return plain data
     *
     * @throws IllegalArgumentException if the password or data are null
     * @throws IllegalStateException if there is an error during the decryption process
     * */
    public byte[] decrypt(char[] password, byte[] cipherMessage){
        if(password == null || password.length == 0){
            throw new IllegalArgumentException("Invalid Password");
        }else if(cipherMessage == null || cipherMessage.length == 0){
            throw new IllegalArgumentException("Invalid Cipher Data");
        }

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
            SecretKey mKey = generateSecretKey(password, salt);

            // Getting IV
            int ivSize = mBuffer.getInt();
            if(ivSize != 12){
                throw new IllegalArgumentException("Invalid IV Size: " + ivSize);
            }
            byte[] iv = new byte[IV_SIZE];
            mBuffer.get(iv);

            // Creating param spec
            GCMParameterSpec mSpec = new GCMParameterSpec(TAG_LENGTH, iv);

            // Getting cipher text
            byte[] msg = new byte[mBuffer.remaining()];
            mBuffer.get(msg);

            byte[] res = null;
            synchronized(mCipher){
                mCipher.init(Cipher.DECRYPT_MODE, mKey, mSpec);
                res = mCipher.doFinal(msg);
            }

            return res;
        }catch(InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){
            throw new IllegalStateException("Unable To Decrypt: " + e.getMessage());
        }
    }
}