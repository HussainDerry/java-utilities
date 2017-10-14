package com.github.hussainderry.test;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.github.hussainderry.ImageCompressionUtils.compressImage;
import static org.junit.Assert.assertTrue;

public class ImageCompressionUtilsTest {

    private static final String SOURCE_IMAGE_PATH_JPG = "./test-images/copperwall-darkdots.jpg";
    private static final String SOURCE_IMAGE_PATH_PNG = "./test-images/programmer-needs-coffee-to-code-hd-desktop-wallpaper.png";

    @Test
    public void testValidJpgCompression() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        final byte[] result = compressImage(source, 0.75f);
        assertTrue(result.length < source.length);
    }

    @Test
    public void testValidPngCompression() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_PNG);
        final byte[] result = compressImage(source, 0.75f);
        assertTrue(result.length < source.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCompressionRatio() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        compressImage(source, 1.15f);
    }

    private byte[] loadImageAsBytes(String path) throws IOException{
        File mSource = new File(path);
        FileInputStream mInputStream = new FileInputStream(mSource);
        ByteArrayOutputStream mFileBytes = new ByteArrayOutputStream();

        byte[] buffer = new byte[64];
        while(mInputStream.read(buffer) != -1){
            mFileBytes.write(buffer);
        }

        return mFileBytes.toByteArray();
    }

}
