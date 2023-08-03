package com.pokemon.daje.util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.core.util.ObjectMapperFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@Component
@Setter
@Slf4j
public class Properties {
    @JsonIgnore
    @Value("${pokemon.daje.external-properties}")
    private static String PATH_TO_PROPERTIES;
    @JsonIgnore
    @Value("${pokemon.daje.external-base-path}")
    private static String EXTERNAL_BASE_PATH;

    public String PATH_TO_FALLBACK_POKEMON;

    public void loadPaths() throws IOException {
        InputStream stream = null;
        Properties properties;
        try{
            log.info(EXTERNAL_BASE_PATH+PATH_TO_PROPERTIES);
            stream = new FileInputStream(EXTERNAL_BASE_PATH+PATH_TO_PROPERTIES);
        }catch (Exception e){
            log.error("PROPERTIES FILE NOT LOADED");
        }
        try{
            if(stream != null){
                properties = ObjectMapperFactory.buildStrictGenericObjectMapper().readValue(stream, Properties.class);
                Properties finalProperties = properties;
                List.of(this.getClass().getFields()).forEach(field -> {
                    try {
                        if(!field.getName().equals("PATH_TO_PROPERTIES")){
                            field.set(this,EXTERNAL_BASE_PATH+ finalProperties.getClass().getField(field.getName()).get(finalProperties));
                        }
                    } catch (Exception e) {
                        log.error("COULD NOT LOAD PROPERTY");
                    }
                });
            }
        }catch (Exception e){
            log.error("FAILED TO SET FIELDS");
        }finally {
            if(stream != null){
                stream.close();
            }
        }
        log.info("THE PROPERTY RESULT FALLBACK POKEMON FOR LOADING PROPERTIES CHECK IS {}",PATH_TO_FALLBACK_POKEMON);
    }
}
