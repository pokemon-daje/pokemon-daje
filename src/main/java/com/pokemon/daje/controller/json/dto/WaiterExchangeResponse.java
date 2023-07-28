package com.pokemon.daje.controller.json.dto;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class WaiterExchangeResponse {
    private HttpServletResponse response;
    private PokemonExchangeDTO pokemonExchangeDTO;

    public void sendData(PokemonExchangeDTO data,int status){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        try{
            response.getWriter().write(String.valueOf(data));
        }catch (Exception ex){
            log.info("POKEMON SEND DATA ERROR");
        }
    }
}
