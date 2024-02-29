package edu.upc.essi.dtim.odin.exception;

public class IntegrityConstraintViolation extends RuntimeException{
    public IntegrityConstraintViolation(String message) {
        super(message);
    }
}
