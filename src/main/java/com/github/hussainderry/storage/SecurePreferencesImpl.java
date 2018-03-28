/*
 * Copyright 2018 Hussain Al-Derry
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

package com.github.hussainderry.storage;

import com.github.hussainderry.crypto.HashSHA;
import com.github.hussainderry.crypto.StringCryptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * @author Hussain Al-Derry <hussain.derry@gmail.com>
 * @version 1.0
 */
public final class SecurePreferencesImpl implements SecurePreferences{

    private final Preferences mPreferences;
    private final StringCryptor mCryptor;

    public SecurePreferencesImpl(String nodeName, PrefSecurityConfig securityConfig) {
        this.mPreferences = Preferences.userRoot().node(nodeName);
        this.mCryptor = StringCryptor.initWithSecurityConfig(securityConfig);
    }

    @Override
    public SecurePreferences putString(String key, String value) {
        mPreferences.put(generateKeyHash(key), encryptToBase64(value));
        return this;
    }

    @Override
    public SecurePreferences putInt(String key, int value) {
        mPreferences.put(generateKeyHash(key), encryptToBase64(Integer.toString(value)));
        return this;
    }

    @Override
    public SecurePreferences putDouble(String key, double value) {
        mPreferences.put(generateKeyHash(key), encryptToBase64(Double.toString(value)));
        return this;
    }

    @Override
    public SecurePreferences putLong(String key, long value) {
        mPreferences.put(generateKeyHash(key), encryptToBase64(Long.toString(value)));
        return this;
    }

    @Override
    public SecurePreferences putBoolean(String key, boolean value) {
        mPreferences.put(generateKeyHash(key), encryptToBase64(Boolean.toString(value)));
        return this;
    }

    @Override
    public Optional<String> getString(String key) {
        String val = mPreferences.get(generateKeyHash(key), null);
        if(val != null){
            return Optional.of(decryptFromBase64(val));
        }else{
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> getInt(String key) {
        String val = mPreferences.get(generateKeyHash(key), null);
        if(val != null){
            try{
                return Optional.of(Integer.parseInt(decryptFromBase64(val)));
            }catch(NumberFormatException e){
                throw new RuntimeException("Requested data type doesn't match the one stored!");
            }
        }else{
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> getDouble(String key) {
        String val = mPreferences.get(generateKeyHash(key), null);
        if(val != null){
            try{
                return Optional.of(Double.parseDouble(decryptFromBase64(val)));
            }catch(NumberFormatException e){
                throw new RuntimeException("Requested data type doesn't match the one stored!");
            }
        }else{
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> getLong(String key) {
        String val = mPreferences.get(generateKeyHash(key), null);
        if(val != null){
            try{
                return Optional.of(Long.parseLong(decryptFromBase64(val)));
            }catch(NumberFormatException e){
                throw new RuntimeException("Requested data type doesn't match the one stored!");
            }
        }else{
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        String val = mPreferences.get(generateKeyHash(key), null);
        if(val != null){
            return Optional.of(Boolean.parseBoolean(decryptFromBase64(val)));
        }else{
            return Optional.empty();
        }
    }

    private String generateKeyHash(String key){
        byte[] hash = HashSHA.hashUsingSHA256(key.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String encryptToBase64(String data){
        return mCryptor.encryptToBase64(data.getBytes(StandardCharsets.UTF_8));
    }

    private String decryptFromBase64(String data){
        byte[] ret = mCryptor.decryptFromBase64(data);
        return new String(ret, StandardCharsets.UTF_8);
    }
}
