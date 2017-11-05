package com.github.hussainderry.test;

import com.github.hussainderry.cache.ConcurrentCache;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConcurrentCacheTest {

    private static ConcurrentCache<Integer, String> mCache;

    @BeforeClass
    public static void init(){
        mCache = new ConcurrentCache<>(2000, 100, 5);
    }

    @Test
    public void testExist(){
        mCache.put(1, "Buffon");
        String value = mCache.get(1);
        Assert.assertNotNull(value);
        Assert.assertEquals(value, "Buffon");
    }

    @Test
    public void testRemoved() throws InterruptedException{
        mCache.put(8, "Marchisio");
        Thread.sleep(2100);
        String value = mCache.get(8);
        Assert.assertNull(value);
    }

}
