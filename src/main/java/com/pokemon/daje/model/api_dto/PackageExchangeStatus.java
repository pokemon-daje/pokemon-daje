package com.pokemon.daje.model.api_dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PackageExchangeStatus {
    @JsonProperty("status_code")
    private int status;
}
