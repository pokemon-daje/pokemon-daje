package com.pokemon.daje.model.functional_programming.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExchangeRequestInteraction {
    private final SwapFunctionAction action;
    private final AsyncContext asyncResponse;
    private Map<ValueEnum, WrapperValue> valuesMap;

    public ExchangeRequestInteraction(SwapFunctionAction action,AsyncContext response) {
        this.action = action;
        this.asyncResponse = response;
        this.valuesMap = new HashMap<>();
    }
    public ExchangeRequestInteraction(SwapFunctionAction action,AsyncContext response, Map<ValueEnum, WrapperValue> values) {
        this.action = action;
        this.asyncResponse = response;
        this.valuesMap = new HashMap<>(values);
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
            log.info("COULD NOT INSTANCIATE PRINTWRITER",ex);
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
            log.info("POKEMON SEND DATA ERROR",ex);
        }
    }
    public SwapFunctionAction getAction(){
        return action;
    };
    public Map<ValueEnum, WrapperValue> getAllValues(){
        return valuesMap;
    }

    public void setValuesMap(Map<ValueEnum, WrapperValue> valuesMap) {
        if(valuesMap != null){
            this.valuesMap = new HashMap<>(valuesMap);
        }
    }
}
