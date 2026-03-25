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
import com.github.hussainderry.crypto.exception.InvalidPasswordException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.github.hussainderry.crypto.Constants.ALGORITHM;
import static com.github.hussainderry.crypto.Constants.BUFFER_SIZE;
import static com.github.hussainderry.crypto.Constants.CIPHER_PARAMS;
import static com.github.hussainderry.crypto.Constants.IV_SIZE;
import static com.github.hussainderry.crypto.Constants.TAG_LENGTH;

/**
 * Used to decrypt files encrypted with {@link FileEncryptorAES}
 * @author Hussain Al-Derry
 */
public final class FileDecryptorAES implements AutoCloseable {

    private final char[] password;
    private ProgressMonitor mProgressMonitor;

    /**
     * Create a new {@link FileDecryptorAES} using a password.
     * Call {@link #close()} when done to zero the internal password copy.
     * @param password The password to use for decryption
     */
    public FileDecryptorAES(char[] password){
        if(password == null){
            throw new IllegalArgumentException("Password cannot be null");
        }
        this.password = Arrays.copyOf(password, password.length);
    }

    @Override
    public void close(){
        Arrays.fill(password, '\0');
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
     * Decrypts the source InputStream and writes it to the OutputStream.
     * Validates the password via an encrypted sentinel before streaming file data.
     *
     * @param mInputStream {@link BufferedInputStream} The source to decrypt
     * @param mOutputStream {@link BufferedOutputStream} The target to write the decrypted data to
     * @return long The number of decrypted bytes written
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws InvalidPasswordException if the password is wrong
     * @throws DecryptionException if an error occurs during decryption
     */
    public long decrypt(BufferedInputStream mInputStream, BufferedOutputStream mOutputStream){
        if(mInputStream == null || mOutputStream == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        byte[] fileIv = readFileIv(mInputStream);
        PBKDF2Helper helper = readPbkdf2Config(mInputStream);

        byte[] key = helper.createKeyFromPassword(password);

        validateSentinel(mInputStream, key);
        return decryptData(mInputStream, mOutputStream, key, fileIv);
    }

    private byte[] readFileIv(InputStream in){
        try{
            byte[] intBuffer = new byte[4];
            readFully(in, intBuffer);
            int ivLength = byteArrayToInt(intBuffer);
            byte[] iv = new byte[ivLength];
            readFully(in, iv);
            return iv;
        }catch(IOException e){
            throw new DecryptionException("Error reading file IV", e);
        }
    }

    private PBKDF2Helper readPbkdf2Config(InputStream in){
        try{
            byte[] intBuffer = new byte[4];
            readFully(in, intBuffer);
            int configLength = byteArrayToInt(intBuffer);
            byte[] config = new byte[configLength];
            readFully(in, config);
            return new PBKDF2Helper.Builder(new String(config, StandardCharsets.UTF_8)).build();
        }catch(IOException e){
            throw new DecryptionException("Error reading PBKDF2 config", e);
        }
    }

    /**
     * Reads and decrypts the sentinel block. If GCM tag verification fails or the
     * decrypted value doesn't match the expected magic, the password is wrong.
     */
    private void validateSentinel(InputStream in, byte[] key){
        try{
            byte[] sentinelIv = new byte[IV_SIZE];
            readFully(in, sentinelIv);

            byte[] intBuffer = new byte[4];
            readFully(in, intBuffer);
            int sentinelLength = byteArrayToInt(intBuffer);
            byte[] sentinelCiphertext = new byte[sentinelLength];
            readFully(in, sentinelCiphertext);

            Cipher sentinelCipher = createCipher();
            initCipher(sentinelCipher, Cipher.DECRYPT_MODE, key, sentinelIv);
            byte[] decrypted = sentinelCipher.doFinal(sentinelCiphertext);

            if(!Arrays.equals(decrypted, Constants.getSentinelPlaintext())){
                throw new InvalidPasswordException("Invalid password");
            }
        }catch(BadPaddingException e){
            throw new InvalidPasswordException("Invalid password", e);
        }catch(IllegalBlockSizeException e){
            throw new DecryptionException("Corrupted sentinel block", e);
        }catch(IOException e){
            throw new DecryptionException("Error reading sentinel", e);
        }
    }

    private long decryptData(BufferedInputStream in, BufferedOutputStream out, byte[] key, byte[] fileIv){
        Cipher aesCipher = createCipher();
        initCipher(aesCipher, Cipher.DECRYPT_MODE, key, fileIv);

        try(CipherInputStream cipherIn = new CipherInputStream(in, aesCipher)){
            long totalBytes = in.available();
            long bytesProcessed = 0;

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while((bytesRead = cipherIn.read(buffer)) != -1){
                out.write(buffer, 0, bytesRead);
                bytesProcessed += bytesRead;
                if(totalBytes > 0){
                    publishProgress((int)((bytesProcessed * 100) / totalBytes));
                }
            }

            out.flush();
            return bytesProcessed;
        }catch(IOException e){
            throw new DecryptionException("IO error during decryption", e);
        }
    }

    private void readFully(InputStream in, byte[] buffer) throws IOException{
        int offset = 0;
        while(offset < buffer.length){
            int read = in.read(buffer, offset, buffer.length - offset);
            if(read == -1){
                throw new IOException("Unexpected end of stream");
            }
            offset += read;
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
            throw new DecryptionException("Unable to initialize cipher", e);
        }
    }

    private void initCipher(Cipher cipher, int mode, byte[] key, byte[] iv){
        try{
            cipher.init(mode, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(TAG_LENGTH, iv));
        }catch(InvalidKeyException | InvalidAlgorithmParameterException e){
            throw new DecryptionException("Unable to init cipher", e);
        }
    }

    private int byteArrayToInt(byte[] b){
        int value = 0;
        for(int i = 0; i < 4; i++){
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}
