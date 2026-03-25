package com.github.hussainderry.crypto.exception;

/**
 * Thrown when a decryption attempt fails due to an incorrect password.
 * @author Hussain Al-Derry
 */
public final class InvalidPasswordException extends CryptoException {

    public InvalidPasswordException(String message){
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause){
        super(message, cause);
    }

}
