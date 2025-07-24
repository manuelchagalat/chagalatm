package com.webinar.copilot.copilot_demo.exception;

public class PokemonApiException extends RuntimeException {
    public PokemonApiException(String message) {
        super(message);
    }

    public PokemonApiException(String message, Throwable cause) {
        super(message, cause);
    }
}