package com.pokemon.daje.util.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PokemonServiceException extends RuntimeException {
    private static final long serialVersionUID = 3813164289998523628L;

    public PokemonServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public PokemonServiceException(String message) {
        super(message);
    }
}
