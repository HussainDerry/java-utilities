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

public class Constants {

    /* IO Config */
    public static final int BUFFER_SIZE = 16;

    /* Encryption Config */
    public static final int IV_SIZE = 16;
    public static final String ALGORITHM = "AES";
    public static final String BLOCK_CHAINING_MODE = "CBC";
    public static final String PADDING_MODE = "PKCS5Padding";
    public static final String CIPHER_PARAMS = String.format("%s/%s/%s", ALGORITHM, BLOCK_CHAINING_MODE, PADDING_MODE);
    public static final String DIGEST_ALGORITHM = "SHA-256";
    public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";

    public static final char SEPARATOR = '.';

}