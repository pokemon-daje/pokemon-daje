package com.pokemon.daje.model;

import java.util.stream.Stream;

public enum ProgressingProcessCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    RESOURCE_NOT_FOUND(404),
    SERVER_ERROR(500),
    UNKWON(121212);

    private int code;
    ProgressingProcessCode(int code) {
        this.code = code;
    }

    public static ProgressingProcessCode fromNumber(int codeStatus) {
        return Stream.of(ProgressingProcessCode.values()).filter(responseCode -> responseCode.code == codeStatus).findFirst().orElse(UNKWON);
    }

    public int getCode(){
        return code;
    }
}
