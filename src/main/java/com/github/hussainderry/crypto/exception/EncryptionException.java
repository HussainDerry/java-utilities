package com.github.hussainderry.crypto.exception;

/**
 * Thrown when an error occurs during encryption.
 * @author Hussain Al-Derry
 */
public final class EncryptionException extends CryptoException {

    public EncryptionException(String message){
        super(message);
    }

    public EncryptionException(String message, Throwable cause){
        super(message, cause);
    }

}
