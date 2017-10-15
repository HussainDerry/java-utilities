package com.github.hussainderry.test;

import com.github.hussainderry.utils.ImageCompressor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class ImageCompressorTest {

    private static final String SOURCE_IMAGE_PATH_JPG = "./test-images/copperwall-darkdots.jpg";
    private static final String SOURCE_IMAGE_PATH_PNG = "./test-images/programmer-needs-coffee-to-code-hd-desktop-wallpaper.png";
    private static ImageCompressor mImageCompressor;

    @BeforeClass
    public static void init(){
        mImageCompressor = new ImageCompressor().setCompressedImageQuality(0.75f);
    }

    @Test
    public void testValidJpgCompression() throws IOException {
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        final byte[] result = mImageCompressor.setSourceImage(source).compressImageToByteArray();

        assertTrue(result.length < source.length);
    }

    @Test
    public void testValidPngCompression() throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_PNG);
        final byte[] result = mImageCompressor.setSourceImage(source).compressImageToByteArray();

        assertTrue(result.length < source.length);
    }

    @Test
    public void testReuse()throws IOException{
        final byte[] source = loadImageAsBytes(SOURCE_IMAGE_PATH_JPG);
        final byte[] result = mImageCompressor.setSourceImage(source).compressImageToByteArray();
        final byte[] result2 = mImageCompressor.compressImageToByteArray();

        assertTrue(Arrays.equals(result, result2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCompressionRatio() throws IOException{
        mImageCompressor.setCompressedImageQuality(1.15f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSource(){
        mImageCompressor.setSourceImage("invalid_base64");
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
