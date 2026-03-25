package com.github.hussainderry.test;

import com.github.hussainderry.cache.ConcurrentCache;
import org.junit.Assert;
import org.junit.Test;

public class ConcurrentCacheTest {

    @Test
    public void cached_entry_is_retrievable_before_ttl_expires(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            cache.put(1, "Buffon");
            String value = cache.get(1);
            Assert.assertNotNull(value);
            Assert.assertEquals("Buffon", value);
        }
    }

    @Test
    public void expired_entry_is_evicted_after_ttl_plus_cleanup() throws InterruptedException{
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(3000, 100, 5)){
            cache.put(8, "Marchisio");
            Thread.sleep(4000);
            String value = cache.get(8);
            Assert.assertNull(value);
        }
    }

    @Test
    public void put_if_absent_returns_null_when_no_prior_mapping(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            String result = cache.putIfAbsent(1, "Buffon");
            Assert.assertNull(result);
        }
    }

    @Test
    public void put_if_absent_returns_existing_value_when_mapping_exists(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            cache.put(1, "Buffon");
            String result = cache.putIfAbsent(1, "Del Piero");
            Assert.assertEquals("Buffon", result);
        }
    }

    @Test
    public void contains_key_true_for_existing_entry(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            cache.put(10, "Del Piero");
            Assert.assertTrue(cache.containsKey(10));
        }
    }

    @Test
    public void contains_key_false_for_missing_entry(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            Assert.assertFalse(cache.containsKey(99));
        }
    }

    @Test
    public void remove_returns_value_and_evicts_entry(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            cache.put(21, "Pirlo");
            String removed = cache.remove(21);
            Assert.assertEquals("Pirlo", removed);
            Assert.assertNull(cache.get(21));
        }
    }

    @Test
    public void remove_returns_null_for_missing_key(){
        try(ConcurrentCache<Integer, String> cache = new ConcurrentCache<>(5000, 1000, 5)){
            Assert.assertNull(cache.remove(99));
        }
    }

}
