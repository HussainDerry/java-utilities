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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Contains helper method to deep clone serializable objects
 * @author Hussain Al-Derry
 */
public final class CloneUtils {

    private CloneUtils(){}

    /**
     * Deep clones an object using streams
     *
     * @param object The object to be cloned, must implement {@link java.io.Serializable}
     * @param clazz The object type
     * @return an exact clone of the given object
     * @throws IllegalArgumentException if any of the arguments is null
     * @throws IllegalStateException If an issue occurs while cloning the object
     */
    public static <T> T deepClone(T object, Class<T> clazz){
        if(object == null){
            throw new IllegalArgumentException("Object cannot be null");
        }
        if(clazz == null){
            throw new IllegalArgumentException("Class cannot be null");
        }
        try{
            ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream mObjectOutputStream = new ObjectOutputStream(mByteArrayOutputStream);
            mObjectOutputStream.writeObject(object);

            ByteArrayInputStream mByteArrayInputStream = new ByteArrayInputStream(mByteArrayOutputStream.toByteArray());
            ObjectInputStream mObjectInputStream = new ObjectInputStream(mByteArrayInputStream);

            return clazz.cast(mObjectInputStream.readObject());
        }catch(IOException | ClassNotFoundException | ClassCastException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
