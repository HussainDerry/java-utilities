package com.github.hussainderry.test;

import com.github.hussainderry.storage.PrefSecurityConfig;
import com.github.hussainderry.storage.SecurePreferences;
import com.github.hussainderry.storage.SecurePreferencesImpl;
import org.junit.BeforeClass;
import org.junit.Test;

public class SecurePreferencesTest {

    private static SecurePreferences mPreferences;

    @BeforeClass
    public static void init(){
        PrefSecurityConfig mConfig = new PrefSecurityConfig.Builder("test-password").build();
        mPreferences = new SecurePreferencesImpl("test-node", mConfig);
    }

    @Test
    public void testString(){
        String str = "test-data";
        mPreferences.putString("string", str);
        assert str.equals(mPreferences.getString("string").get());
    }

    @Test
    public void testInteger(){
        int val = 567;
        mPreferences.putInt("integer", val);
        assert val == mPreferences.getInt("integer").get();
    }

    @Test
    public void testBoolean(){
        mPreferences.putBoolean("bool", true);
        assert mPreferences.getBoolean("bool").get();
    }

}
