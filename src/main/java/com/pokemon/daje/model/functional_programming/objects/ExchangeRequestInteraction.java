package com.pokemon.daje.model.functional_programming.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExchangeRequestInteraction {
    private final SwapFunctionAction action;
    private final AsyncContext asyncResponse;
    private Map<ValueEnum, ValueWrapper> valuesMap;

    public ExchangeRequestInteraction(SwapFunctionAction action,AsyncContext response) {
        this.action = action;
        this.asyncResponse = response;
        this.valuesMap = new EnumMap<>(ValueEnum.class);
    }
    public ExchangeRequestInteraction(SwapFunctionAction action,AsyncContext response, Map<ValueEnum, ValueWrapper> values) {
        this.action = action;
        this.asyncResponse = response;
        this.valuesMap = new EnumMap<>(ValueEnum.class);
        valuesMap.putAll(values);
    }

    public <D extends Object> void sendData(D data,int status){
        asyncResponse.getResponse().setContentType("text/json");
        asyncResponse.getResponse().setCharacterEncoding("UTF-8");
        HttpServletResponse response = (HttpServletResponse) asyncResponse.getResponse();
        response.setStatus(status);
        PrintWriter out = null;
        try {
            out = asyncResponse.getResponse().getWriter();
        } catch (IOException ex) {
            log.error("COULD NOT INSTANCE PRINTWRITER",ex);
        }
        try{
            if(data!=null && out != null){
                out.write(new ObjectMapper().writeValueAsString(data));
                out.flush();
            }else if(out != null){
                out.write("{}");
                out.flush();
            }
            asyncResponse.complete();
        }catch (Exception ex){
            if(out != null){
                out.write("{}");
                out.flush();
            }
            log.error("POKEMON SEND DATA ERROR",ex);
        }
    }
    public SwapFunctionAction getAction(){
        return action;
    };
    public Map<ValueEnum, ValueWrapper> getAllValues(){
        return valuesMap;
    }

    public void setValuesMap(Map<ValueEnum, ValueWrapper> valuesMap) {
        if(valuesMap != null){
            this.valuesMap = new EnumMap<>(ValueEnum.class);
            this.valuesMap.putAll(valuesMap);
        }
    }
}
