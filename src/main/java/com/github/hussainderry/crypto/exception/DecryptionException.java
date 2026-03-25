package com.github.hussainderry.crypto.exception;

/**
 * Thrown when an error occurs during decryption (corrupted data, IO failure).
 * @author Hussain Al-Derry
 */
public final class DecryptionException extends CryptoException {

    public DecryptionException(String message){
        super(message);
    }

    public DecryptionException(String message, Throwable cause){
        super(message, cause);
    }

}
