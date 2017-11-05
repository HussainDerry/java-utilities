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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static com.github.hussainderry.crypto.Constants.KEY_DERIVATION_ALGORITHM;

/**
 * Wrapper class to help manipulate PBKDF2 parameters and generate keys
 * @author Hussain Al-Derry
 */
public class PBKDF2Helper {

    private final SecureRandom mRandom;
    private final int iterations;
    private final int saltSize;
    private final int keySize;
    private byte[] salt;

    /**
     * Creates a new instance with the given parameters
     * @param keySize The size of the key to be generated
     * @param saltSize The PBKDF2 salt size
     * @param iterations The PBKDF2 iterations
     */
    public PBKDF2Helper(int keySize, int saltSize, int iterations) {
        this.iterations = iterations;
        this.saltSize = saltSize;
        this.keySize = keySize;
        this.mRandom = new SecureRandom();
    }

    /**
     * Creates a new instance with the given parameters
     * @param keySize The size of the key to be generated
     * @param iterations The PBKDF2 iterations
     * @param salt The PBKDF2 salt
     */
    public PBKDF2Helper(int keySize, int iterations, byte[] salt) {
        this.iterations = iterations;
        this.saltSize = salt.length;
        this.keySize = keySize;
        this.salt = Arrays.copyOf(salt, salt.length);
        this.mRandom = new SecureRandom();
    }

    /**
     * Creates key by using PBKDF2 on the given password
     * @param password The password to use
     * @return key as byte array
     */
    public byte[] createKeyFromPassword(String password){
        generateInitialSalt();
        PBEKeySpec mSpec = new PBEKeySpec(password.toCharArray(), salt, iterations, keySize);
        try {
            SecretKeyFactory mKeyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            return mKeyFactory.generateSecret(mSpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to create encryption key", e);
        }
    }

    /**
     * @return true if the salt has been generated, else false
     */
    public boolean saltGenerated(){
        return salt != null;
    }

    /**
     * @return PBKDF2 salt as byte array
     * @throws IllegalStateException if the salt hasn't been generated yet
     */
    public byte[] getSalt(){
        if(salt == null){
            throw new IllegalStateException("Salt hasn't been generated yet!");
        }
        return Arrays.copyOf(salt, salt.length);
    }

    /**
     * @return PBKDF2 salt as HEX string
     * @throws IllegalStateException if the salt hasn't been generated yet
     */
    public String getSaltAsHexString(){
        return new BigInteger(getSalt()).toString(16);
    }

    /**
     * @return PBKDF2 Base64 encoded salt
     * @throws IllegalStateException if the salt hasn't been generated yet
     */
    public String getSaltAsBase64String(){
        return Base64.encodeBase64String(getSalt());
    }

    /**
     * Returns the current PBKDF2 configurations which can be provided to the Builder for easy initialization.
     * @return String PBKDF2 configurations
     */
    public String getPbkdf2Configurations(){
        return String.format("%s:%s:%s:%s", this.keySize, this.saltSize, this.iterations, getSaltAsHexString());
    }

    private void generateInitialSalt(){
        if(!saltGenerated()){
            salt = new byte[saltSize];
            mRandom.nextBytes(salt);
        }
    }

    /**
     * Builder for PBKDF2Helper
     */
    public static class Builder{

        private int keySize;
        private int saltSize = 32;
        private int iterations = 1000;
        private byte[] salt = null;

        /**
         * Creates a new builder object with the given key size
         * @param keySize key size
         */
        public Builder(KeySize keySize){
            this.keySize = keySize.getValue();
        }

        /**
         * Creates a builder instance by initializing the params from a PBKDF2 configurations string
         * @param pbkdf2Configurations PBKDF2 configurations to use
         * @throws IllegalArgumentException if configurations are invalid
         */
        public Builder(String pbkdf2Configurations){
            String[] parts = pbkdf2Configurations.split(":");
            if(parts.length != 4){
                throw new IllegalArgumentException("Malformed Configurations");
            }

            try{
                this.keySize = Integer.parseInt(parts[0]);
                this.saltSize = Integer.parseInt(parts[1]);
                this.iterations = Integer.parseInt(parts[2]);
                this.salt = new BigInteger(parts[3], 16).toByteArray();
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("Malformed Configurations");
            }
        }

        /**
         * @param saltSize The PBKDF2 salt size
         * @return The modified builder instance
         */
        public Builder saltSize(SaltSize saltSize){
            this.saltSize = saltSize.getValue();
            return this;
        }

        /**
         * @param iterations The PBKDF2 iterations
         * @return The modified builder instance
         */
        public Builder iterations(Iterations iterations){
            this.iterations = iterations.getValue();
            return this;
        }

        /**
         * Creates a {@link PBKDF2Helper} using the current builder configurations
         * @return The configured PBKDF2Helper instance
         */
        public PBKDF2Helper build(){
            if(this.salt == null){
                return new PBKDF2Helper(this.keySize, this.iterations, this.saltSize);
            }else{
                return new PBKDF2Helper(this.keySize, this.iterations, this.salt);
            }
        }

    }
}
