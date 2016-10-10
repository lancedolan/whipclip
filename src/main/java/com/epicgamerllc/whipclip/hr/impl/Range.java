package com.epicgamerllc.whipclip.hr.impl;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author lance
 */
public class Range {
    
    private Set<Character> chars = new HashSet<>();
    
    //Either Character.UPPERCASE_LETTER, Character.DECIMAL_DIGIT_NUMBER, 
    //or Character.LOWERCASE_LETTER
    private int type;
    
    /**
     * 
     * @param range Alpha or numeric range in [a-z] , [A-Z], or [0-9] format.
     */
    public Range(final String range) {
        int lowerBound = -1;
        int upperBound = -1;
        for (char c : range.toCharArray()) {
            if (isAlphaNumeric(c)) {
                if (lowerBound == -1) {
                    lowerBound = c;
                } else {
                    upperBound = c;
                    break;
                }
            }
        }
        initRange(lowerBound, upperBound);
    }
    
    /*
     * returns true if character is alphanumeric, while also
     * using this information to accurately set this.type
     */
    private boolean isAlphaNumeric(char c) {
        switch (Character.getType(c)) {
            case Character.UPPERCASE_LETTER:
                this.type = Character.UPPERCASE_LETTER;
                return true;
            case Character.LOWERCASE_LETTER:
                this.type = Character.LOWERCASE_LETTER;
                return true;
            case Character.DECIMAL_DIGIT_NUMBER:
                this.type = Character.DECIMAL_DIGIT_NUMBER;
                return true;
            default:
                return false;
        }
    }
    /**
     * populate this.chars with the characters representing between lowerBound
     * and upperBound, inclusively. 
     */
    private void initRange(final int lowerBound , final int upperBound) {
        for (int i = lowerBound; i <= upperBound ; i++) {
            this.chars.add(toChar(i));
        }
    }
    
    private Character toChar(final int integer) {
        if (this.type == Character.DECIMAL_DIGIT_NUMBER) {
            return Integer.toString(integer).charAt(0);
        }
        //Can safely assume it is upper or lower case character
        return (char)integer;
    }

    public Set<Character> getChars() {
        return chars;
    }

}
