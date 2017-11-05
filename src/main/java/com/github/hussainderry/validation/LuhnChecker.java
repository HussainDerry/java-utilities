/*
 * Copyright 2017 Hussain Al-Derry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hussainderry.validation;

/**
 * A helper class to check number validity using the Luhn algorithm.
 * @author Hussain Al-Derry
 */
public class LuhnChecker {

    private static final String INPUT_ERROR = "Input must contain only digits (0-9), no characters or whitespaces";

    /**
     * Checks number's validity using the last digit as a check digit.
     * @param number The number to check
     * @return boolean whether the number is valid or not.
     * */
    public static boolean checkNumberValidity(String number){
        int[] numbers;

        try{
             numbers = stringToIntArray(number);
        }catch(NumberFormatException e){
            throw new IllegalArgumentException(INPUT_ERROR);
        }

        int finalDigit = numbers[numbers.length - 1];

        for(int j = numbers.length - 2; j >= 0; j-=2){
            numbers[j] *= 2;
            if(numbers[j] > 9){
                numbers[j] -= 9;
            }
        }

        int totalSum = 0;
        for(int n = 0; n < numbers.length - 1; n++){
            totalSum += numbers[n];
        }

        return ((totalSum * 9) % 10) == finalDigit;
    }

    private static int[] stringToIntArray(String str){
        int[] numbers = new int[str.length()];
        for(int i = str.length() - 1; i >= 0; i--) {
            numbers[i] = Integer.parseInt(str.substring(i, i + 1));
        }

        return numbers;
    }
}
