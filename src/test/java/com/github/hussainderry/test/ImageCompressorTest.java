package com.github.hussainderry.test;

import com.github.hussainderry.utils.ImageCompressor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class ImageCompressorTest {

    private static final String SOURCE_IMAGE_PATH_JPG = "test-images/copperwall-darkdots.jpg";
    private static final String SOURCE_IMAGE_PATH_PNG = "test-images/programmer-needs-coffee-to-code-hd-desktop-wallpaper.png";
    private ImageCompressor mImageCompressor;

    @Before
    public void init(){
        mImageCompressor = new ImageCompressor().setCompressedImageQuality(0.75f);
    }

    @Test
    public void jpg_compressed_to_byte_array_is_smaller_than_source() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        final byte[] result = mImageCompressor.setSourceImage(source).compressImageToByteArray();
        assertTrue(result.length < source.length);
    }

    @Test
    public void png_compressed_to_byte_array_is_smaller_than_source() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_PNG);
        final byte[] result = mImageCompressor.setSourceImage(source).compressImageToByteArray();
        assertTrue(result.length < source.length);
    }

    @Test
    public void compressing_same_source_twice_produces_identical_output() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        final byte[] result = mImageCompressor.setSourceImage(source).compressImageToByteArray();
        final byte[] result2 = mImageCompressor.compressImageToByteArray();
        assertTrue(Arrays.equals(result, result2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void quality_above_one_throws_illegal_argument(){
        mImageCompressor.setCompressedImageQuality(1.15f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_base64_source_throws_illegal_argument(){
        mImageCompressor.setSourceImage("invalid_base64");
    }

    private byte[] loadImageAsBytes(String resourcePath) throws IOException{
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)){
            if(is == null){
                throw new IOException("Resource not found: " + resourcePath);
            }
            return is.readAllBytes();
        }
    }

}
