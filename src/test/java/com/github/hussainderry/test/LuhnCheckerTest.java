package com.github.hussainderry.test;

import com.github.hussainderry.LuhnChecker;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LuhnCheckerTest {

    private static final String VALID_CARD_NUMBER = "4532136548631895";
    private static final String INVALID_CARD_NUMBER = "4532136548630000";
    private static final String INVALID_INPUT = "abc4532136548";

    @Test
    public void testValidCardNumber(){
        assertTrue(LuhnChecker.checkNumberValidity(VALID_CARD_NUMBER));
    }

    @Test
    public void testInvalidCardNumber(){
        assertFalse(LuhnChecker.checkNumberValidity(INVALID_CARD_NUMBER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInput(){
        LuhnChecker.checkNumberValidity(INVALID_INPUT);
    }

}
