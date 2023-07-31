package com.pokemon.daje.model.business_data;

import java.util.stream.Stream;

public enum ProgressingProcessCode {
    POKEMON_EXCHANGE_REQUEST_OPEN(0),
    POKEMON_REQUEST_SUCCESS(200),
    POKEMON_BAD_REQUEST(400),
    POKEMON_EXCHANGE_NOT_FOUND(404),
    POKEMON_REQUEST_DOWN_SERVER_ERROR(500),
    POKEMON_REQUEST_UNKWON(121212);

    private int code;
    ProgressingProcessCode(int code) {
        this.code = code;
    }

    public static ProgressingProcessCode fromNumber(int codeStatus) {
        return Stream.of(ProgressingProcessCode.values()).filter(responseCode -> responseCode.code == codeStatus).findFirst().orElse(POKEMON_REQUEST_UNKWON);
    }

    public int getCode(){
        return code;
    }
}
