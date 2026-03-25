package com.github.hussainderry.test;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.github.hussainderry.utils.ImageCompressionUtils.compressImage;
import static org.junit.Assert.assertTrue;

public class ImageCompressionUtilsTest {

    private static final String SOURCE_IMAGE_PATH_JPG = "test-images/copperwall-darkdots.jpg";
    private static final String SOURCE_IMAGE_PATH_PNG = "test-images/programmer-needs-coffee-to-code-hd-desktop-wallpaper.png";

    @Test
    public void jpg_compressed_output_is_smaller_than_source() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        final byte[] result = compressImage(source, 0.75f);
        assertTrue(result.length < source.length);
    }

    @Test
    public void png_compressed_output_is_smaller_than_source() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_PNG);
        final byte[] result = compressImage(source, 0.75f);
        assertTrue(result.length < source.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void quality_above_one_throws_illegal_argument() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        compressImage(source, 1.15f);
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
