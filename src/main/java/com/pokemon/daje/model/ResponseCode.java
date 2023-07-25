package com.pokemon.daje.model;

public enum ResponseCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    SERVER_ERROR(500);

    private int code;
    ResponseCode(int code) {
        this.code = code;
    }
    public int getCode(){
        return code;
    }
}
