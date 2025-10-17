package io.github.haroldbaes17.minecraftfacts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoleNotDeletedException extends RuntimeException {
    public RoleNotDeletedException(String message) {
        super(message);
    }
}
