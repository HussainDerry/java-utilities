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

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import static com.github.hussainderry.crypto.Constants.*;

/**
 * Used to decrypt files encrypted with {@link FileEncryptorAES}
 * @author Hussain Al-Derry
 */
public class FileDecryptorAES {

    private final Cipher mAesCipher;
    private final MessageDigest mDigest;
    private final String password;
    private ProgressMonitor mProgressMonitor;
    private PBKDF2Helper mHelper;
    private byte[] iv;
    private byte[] key;

    /**
     * Create a new {@link FileDecryptorAES} using a password
     * @param password The password to use for decryption
     * @throws RuntimeException if there is an error initializing the cipher
     */
    public FileDecryptorAES(String password){
        this.password = password;
        try {
            this.mAesCipher = Cipher.getInstance(CIPHER_PARAMS);
            this.mDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Unable to initialize cipher", e);
        }
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
     * Decrypts the source InputStream and writes it to the OutputStream
     * @param mInputStream {@link BufferedInputStream} The source to decrypt
     * @param mOutputStream {@link BufferedOutputStream} The target to write the decrypted data to
     * @return long The number of bytes decrypted
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws RuntimeException if there is an IO exception
     */
    public long decrypt(BufferedInputStream mInputStream, BufferedOutputStream mOutputStream){
        if(mInputStream == null || mOutputStream == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        synchronized(mAesCipher){
            loadEncryptionParams(mInputStream);
            initParams(password);
            setModeDecrypt();

            try(InflaterInputStream mInflaterInputStream = new InflaterInputStream(mInputStream);
                CipherInputStream mAesInputStream = new CipherInputStream(mInflaterInputStream, mAesCipher)){

                int itr = mInputStream.available() / BUFFER_SIZE;
                int counter = 0;

                byte[] buffer = new byte[BUFFER_SIZE];
                while(mAesInputStream.read(buffer) != -1){
                    mOutputStream.write(buffer);
                    publishProgress((counter++ * 100) / itr);
                }

                mOutputStream.flush();
                return counter * BUFFER_SIZE;
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initialize the encryption parameters
     * @param password The base password
     */
    private void initParams(String password){
        this.key = mHelper.createKeyFromPassword(password);
    }

    /**
     * Loads the encryption params from the encrypted file
     * @param mInputStream The input stream of the encrypted file
     * @throws RuntimeException if an IO exception occurs
     */
    private void loadEncryptionParams(InputStream mInputStream){
        try{

            // Loading password checksum
            String checksumBase64 = readToNextSeparator(mInputStream);
            byte[] checksum = Base64.decodeBase64(checksumBase64);
            if(!validatePasswordChecksum(checksum)){
                throw new IllegalStateException("Invalid password");
            }

            // Loading IV
            String ivBase64 = readToNextSeparator(mInputStream);
            this.iv = Base64.decodeBase64(ivBase64);

            // Loading PBKDF2 configurations
            String config = new String(Base64.decodeBase64(readToNextSeparator(mInputStream)), StandardCharsets.UTF_8);
            this.mHelper = new PBKDF2Helper.Builder(config).build();

        }catch(IOException e) {
            throw new RuntimeException("Error reading params", e);
        }
    }

    /**
     * Loads the next encryption parameter from the encrypted file
     * @param mInputStream The encrypted file input stream
     * @return String base64 encoded param
     * @throws IOException If an IO error occurs
     */
    private String readToNextSeparator(final InputStream mInputStream) throws IOException{
        int r;
        StringBuilder mStringBuilder = new StringBuilder();
        while((r = mInputStream.read()) != -1){
            char temp = (char) r;
            if(temp != SEPARATOR){
                mStringBuilder.append(temp);
            }else{
                break;
            }
        }
        return mStringBuilder.toString();
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
    private void setModeDecrypt()  {
        try {
            mAesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new IvParameterSpec(iv));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Unable to init encryption mode", e);
        }
    }

    /**
     * Checks if the provided checksum matches the one corresponding to the current set password
     * @param fileChecksum The password checksum loaded from the encrypted file
     * @return true if the checksums match
     */
    private boolean validatePasswordChecksum(final byte[] fileChecksum){
        byte[] passwordChecksum = mDigest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Arrays.equals(passwordChecksum, fileChecksum);
    }
}
