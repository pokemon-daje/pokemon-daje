package com.pokemon.daje.controller.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PackageExchangeStatus {
    @JsonProperty("status_code")
    private int status;
}
