package com.smecs.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        // At least 8 chars
        // At least one digit
        // At least one lower case
        // At least one upper case
        // At least one special char (@#$%^&+=! etc)
        // No whitespace
        return password.length() >= 8 &&
               password.matches(".*\\d.*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[@#$%^&+=!].*") &&
               !password.matches(".*\\s.*");
    }
}
