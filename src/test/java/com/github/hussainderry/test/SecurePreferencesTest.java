package com.github.hussainderry.test;

import com.github.hussainderry.storage.SecurePreferences;
import com.github.hussainderry.storage.SecurePreferencesImpl;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hussain Al-Derry <hussain.derry@gmail.com>
 */
public class SecurePreferencesTest {

	private static SecurePreferences mPreferences;

	@BeforeClass
	public static void init(){
		mPreferences = new SecurePreferencesImpl("test-node", "test-password");
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
