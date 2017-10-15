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
package com.github.hussainderry.utils;

import java.io.*;

/**
 * Contains helper method to deep clone serializable objects
 * @author Hussain Al-Derry
 */
public class CloneUtils {

    /**
     * Deep clones an object using streams
     *
     * @param object The object to be cloned, must implement {@link Serializable}
     * @param clazz The object type
     * @return an exact clone of the given object
     * @throws IllegalStateException If an issue occurs while cloning the object
     */
    public static <T> T deepClone(T object, Class<T> clazz){
        try{
            // Write provided object bytes to OutputStream
            ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream mObjectOutputStream = new ObjectOutputStream(mByteArrayOutputStream);
            mObjectOutputStream.writeObject(object);

            // Reading bytes via a new InputStream
            ByteArrayInputStream mByteArrayInputStream = new ByteArrayInputStream(mByteArrayOutputStream.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(mByteArrayInputStream);

            // Creating the clone object
            return clazz.cast(ois.readObject());
        }catch(IOException | ClassNotFoundException | ClassCastException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
