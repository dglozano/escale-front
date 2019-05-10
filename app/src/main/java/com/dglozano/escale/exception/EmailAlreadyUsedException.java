package com.dglozano.escale.exception;

public class EmailAlreadyUsedException extends Exception {

    public EmailAlreadyUsedException(String email) {
        super(String.format("Ups! Ya hay un usuario registrado con el email %s.", email));
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
