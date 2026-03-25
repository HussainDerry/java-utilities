package com.github.hussainderry.test;

import com.github.hussainderry.storage.SecurePreferences;
import com.github.hussainderry.storage.SecurePreferencesImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Hussain Al-Derry
 */
public class SecurePreferencesTest {

	private static final String NODE_NAME = "java-utilities-test-node";
	private static final char[] PASSWORD = "test-password".toCharArray();
	private SecurePreferences mPreferences;

	@Before
	public void init() throws BackingStoreException{
		Preferences root = Preferences.userRoot();
		if(root.nodeExists(NODE_NAME)){
			root.node(NODE_NAME).removeNode();
			root.flush();
		}
		mPreferences = new SecurePreferencesImpl(NODE_NAME, PASSWORD);
	}

	@After
	public void cleanup() throws Exception{
		mPreferences.close();
		Preferences root = Preferences.userRoot();
		if(root.nodeExists(NODE_NAME)){
			root.node(NODE_NAME).removeNode();
			root.flush();
		}
	}

	@Test
	public void string_value_survives_encrypt_decrypt_round_trip(){
		String str = "test-data";
		mPreferences.putString("string", str);
		Assert.assertEquals(str, mPreferences.getString("string").get());
	}

	@Test
	public void integer_value_survives_encrypt_decrypt_round_trip(){
		int val = 567;
		mPreferences.putInt("integer", val);
		Assert.assertEquals(val, (int) mPreferences.getInt("integer").get());
	}

	@Test
	public void boolean_value_survives_encrypt_decrypt_round_trip(){
		mPreferences.putBoolean("bool", true);
		Assert.assertTrue(mPreferences.getBoolean("bool").get());
	}

	@Test
	public void double_value_survives_encrypt_decrypt_round_trip(){
		double val = 3.14159265358979;
		mPreferences.putDouble("pi", val);
		Assert.assertEquals(val, mPreferences.getDouble("pi").get(), 0.0);
	}

	@Test
	public void long_value_survives_encrypt_decrypt_round_trip(){
		long val = 9_223_372_036_854_775_807L;
		mPreferences.putLong("maxLong", val);
		Assert.assertEquals(Long.valueOf(val), mPreferences.getLong("maxLong").get());
	}

	@Test
	public void missing_key_returns_empty_optional(){
		Assert.assertFalse(mPreferences.getString("nonexistent-key").isPresent());
	}

}
