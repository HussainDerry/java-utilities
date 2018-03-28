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

import java.util.Optional;

/**
 * @author Hussain Al-Derry
 * @version 1.0
 */
public interface SecurePreferences {

    SecurePreferences putString(String key, String value);
    SecurePreferences putInt(String key, int value);
    SecurePreferences putDouble(String key, double value);
    SecurePreferences putLong(String key, long value);
    SecurePreferences putBoolean(String key, boolean value);

    Optional<String> getString(String key);
    Optional<Integer> getInt(String key);
    Optional<Double> getDouble(String key);
    Optional<Long> getLong(String key);
    Optional<Boolean> getBoolean(String key);

}
