package com.github.hussainderry.crypto.exception;

/**
 * Thrown when PBKDF2 key derivation fails.
 * @author Hussain Al-Derry
 */
public final class KeyDerivationException extends CryptoException {

    public KeyDerivationException(String message){
        super(message);
    }

    public KeyDerivationException(String message, Throwable cause){
        super(message, cause);
    }

}
