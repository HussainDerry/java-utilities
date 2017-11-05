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

import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.github.hussainderry.utils.ImageCompressionUtils.compressImage;

/**
 * A Thread-Safe Object-Oriented wrapper for the {@link ImageCompressionUtils} utility class with Base64 support.
 * The default compressed image quality is 0.5
 * @author Hussain Al-Derry
 */
public class ImageCompressor {

    private static final String ILLEGAL_COMPRESSED_IMAGE_QUALITY = "Illegal compressed image quality (quality must be >= 0 and <= 1)";
    private static final String NULL_BYTES = "Data cannot be null";
    private final Base64.Decoder mDecoder;
    private final Base64.Encoder mEncoder;
    private final ReadWriteLock mReadWriteLock;

    private byte[] mImageBytes = null;
    private float quality = 0.50f;

    public ImageCompressor(){
        this.mEncoder = Base64.getEncoder();
        this.mDecoder = Base64.getDecoder();
        this.mReadWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Set the quality of the compressed image
     * @param quality The compressed image quality, must be between 0 and 1 (inclusive)
     * @return The {@link ImageCompressor} instance being used, to enable method chaining
     * @throws IllegalArgumentException if the quality value is not valid
     */
    public ImageCompressor setCompressedImageQuality(final float quality){
        if(!isValidImageQuality(quality)){
            throw new IllegalArgumentException(ILLEGAL_COMPRESSED_IMAGE_QUALITY);
        }
        this.quality = quality;
        return this;
    }

    /**
     * Set the image to compress
     * @param imageBytes The image bytes
     * @return The {@link ImageCompressor} instance being used, to enable method chaining
     * @throws IllegalArgumentException if the image bytes are null
     */
    public ImageCompressor setSourceImage(final byte[] imageBytes){
        mReadWriteLock.readLock().lock();
        if(imageBytes != null){
            mReadWriteLock.readLock().unlock();
            mReadWriteLock.writeLock().lock();
            mImageBytes = Arrays.copyOf(imageBytes, imageBytes.length);
            mReadWriteLock.writeLock().unlock();
            return this;
        }else{
            mReadWriteLock.readLock().unlock();
            throw new IllegalArgumentException(NULL_BYTES);
        }
    }

    /**
     * Set the image to compress
     * @param imageBase64 The image bytes encoded in Base64
     * @return The {@link ImageCompressor} instance being used, to enable method chaining
     * @throws IllegalArgumentException if the image bytes are null or is not a valid Base64 string
     */
    public ImageCompressor setSourceImage(final String imageBase64){
        mReadWriteLock.readLock().lock();
        if(imageBase64 != null){
            mReadWriteLock.readLock().unlock();
            mReadWriteLock.writeLock().lock();
            mImageBytes = mDecoder.decode(imageBase64);
            mReadWriteLock.writeLock().unlock();
            return this;
        }else{
            mReadWriteLock.readLock().unlock();
            throw new IllegalArgumentException(NULL_BYTES);
        }
    }

    /**
     * Compress the image as Base64 string
     * @return The compressed image encoded as Base64 string
     * @throws IllegalStateException if the image data has not been set, or an internal error occurs
     */
    public String compressImageToBase64(){
        return mEncoder.encodeToString(compressImageToByteArray());
    }

    /**
     * Compress the image as byte array
     * @return The compressed image as byte array
     * @throws IllegalStateException if the image data has not been set, or an internal error occurs
     */
    public byte[] compressImageToByteArray(){
        mReadWriteLock.readLock().lock();
        final byte[] compressed = compressImage(mImageBytes, quality);
        mReadWriteLock.readLock().unlock();
        return compressed;
    }

    private static boolean isValidImageQuality(float ratio){
        return ratio >= 0 && ratio <= 1;
    }


}
