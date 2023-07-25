package com.pokemon.daje.controller.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageFrontEnd {
    @JsonProperty("exchange_id")
    private String exchangeId;
    @JsonProperty("status_code")
    private int status;

    public PackageFrontEnd(String exchangeId, int status) {
        this.exchangeId = exchangeId;
        this.status = status;
    }
}
