package com.github.hussainderry.crypto.exception;

/**
 * Base exception for all cryptographic operations.
 * @author Hussain Al-Derry
 */
public class CryptoException extends RuntimeException {

    public CryptoException(String message){
        super(message);
    }

    public CryptoException(String message, Throwable cause){
        super(message, cause);
    }

}
