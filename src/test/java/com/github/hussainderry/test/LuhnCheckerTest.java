package com.github.hussainderry.test;

import com.github.hussainderry.validation.LuhnChecker;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LuhnCheckerTest {

    private static final String VALID_CARD_NUMBER = "4532136548631895";
    private static final String INVALID_CARD_NUMBER = "4532136548630000";
    private static final String INVALID_INPUT = "abc4532136548";

    @Test
    public void valid_card_number_passes_luhn_check(){
        assertTrue(LuhnChecker.checkNumberValidity(VALID_CARD_NUMBER));
    }

    @Test
    public void invalid_card_number_fails_luhn_check(){
        assertFalse(LuhnChecker.checkNumberValidity(INVALID_CARD_NUMBER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void non_numeric_input_throws_illegal_argument(){
        LuhnChecker.checkNumberValidity(INVALID_INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_input_throws_illegal_argument(){
        LuhnChecker.checkNumberValidity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty_string_throws_illegal_argument(){
        LuhnChecker.checkNumberValidity("");
    }

}
