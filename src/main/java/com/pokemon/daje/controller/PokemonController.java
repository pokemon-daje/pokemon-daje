package com.pokemon.daje.controller;

import com.pokemon.daje.controller.json.dto.*;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.model.ProgressingProcessCode;
import com.pokemon.daje.service.PokemonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class PokemonController {
    final PokemonService pokemonService;
    private Set<SseEmitter> serverEmitters;

    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
        serverEmitters =new HashSet<>();
    }

    @GetMapping("/pokemon")
    public ResponseEntity<List<PokemonFrontEndDTO>> getSixRandom() {
        return ResponseEntity.ok(pokemonService.getSixRandomPokemon());
    }
    @GetMapping("/pokemon/{id}")
    public ResponseEntity<PokemonFrontEndDTO> getById(@PathVariable int id) {
        return ResponseEntity.ok(pokemonService.getById(id));
    }
    @PostMapping("/pokemon")
    public void insert(@RequestBody Pokemon pokemon) {
        pokemonService.insert(pokemon);
    }
    @DeleteMapping("/pokemon/{id}")
    public void delete(@PathVariable int id) {
        pokemonService.deleteById(id);
    }

    @PostMapping("/pokemon/exchange")
    public ResponseEntity<PackageExchange> swap(@RequestBody PokemonExchangeDTO pokemon) {
        PackageExchange pack = pokemonService.inizializePokemonsSwap(pokemon);
        ResponseEntity<PackageExchange> toSend = new ResponseEntity<>(pack,HttpStatus.OK);
        if(pack != null){
            sendDataToFrontEnd(pack.getId(),0);
            toSend = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return toSend;
    }
    @GetMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorGet(@RequestBody PokemonExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @PutMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorPut(@RequestBody PokemonExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @DeleteMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorDelete(@RequestBody PokemonExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping("/pokemon/exchange/{exchangeId}/status")
    public ResponseEntity<HttpStatus> statusSwap(@PathVariable("exchangeId") String exchangeId, @RequestBody PackageExchangeStatus packageExchangeStatus){
        ProgressingProcessCode code;
        if(!ObjectUtils.isEmpty(exchangeId) && packageExchangeStatus != null
                && !ProgressingProcessCode.UNKWON.equals(ProgressingProcessCode.fromNumber(packageExchangeStatus.getStatus()))
        ){
            code = pokemonService.nextStepSwap(exchangeId,packageExchangeStatus);
        } else {
            code = ProgressingProcessCode.BAD_REQUEST;
        }
        sendDataToFrontEnd(exchangeId,code.getCode());
        switch (code){
            case SUCCESS -> {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            case BAD_REQUEST -> {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            case RESOURCE_NOT_FOUND -> {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            default -> {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping("/pokemon/exchange/events")
    public SseEmitter streamSseMvc() {
        SseEmitter emitter = new SseEmitter(1000L);
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data(new Date())
                        .id("exchange")
                        .name("pokemon");
                emitter.send(event);

                serverEmitters.add(emitter);
                emitter.onCompletion(()->{serverEmitters.remove(emitter);});
                emitter.onTimeout(emitter::complete);

            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        return emitter;
    }

//    @PostMapping("/pokemon/getpackage")
//    public void getPackagePokemons(@RequestBody List<GatherDataPokemonSpecie> pokemonExchangeDTOList) throws IOException {
//        int ok =0;
//        StringBuilder scriptBuilder = new StringBuilder().append("insert into pokemon_species(pokedex_id,type_id,name,sprite_url) values \n");
//        pokemonExchangeDTOList.forEach(pokemon -> {
//            scriptBuilder.append("(").append(pokemon.getId()+",")
//                    .append(TypesEnum.fromString(pokemon.getType()).getId()+",")
//                    .append("'"+pokemon.getName()+"'"+",")
//                    .append("'"+pokemon.getSprite()+"'")
//                    .append("), \n");
//        });
//        File newFile = new File("D:/download/insertPokemonSpecies.sql");
//        FileWriter write = new FileWriter(newFile);
//        write.write(scriptBuilder.toString());
//        write.close();
//    }
//
//    @PostMapping("/pokemon/getpackage/moves")
//    public void getPackageMoves(@RequestBody List<GatherDataPokemonMove> pokemonMoveExchangeDTOList) throws IOException {
//        int ok =0;
//        StringBuilder scriptBuilder = new StringBuilder().append("insert into move(pokedex_move_id,type_id,name,power) values \n");
//        pokemonMoveExchangeDTOList.forEach(pokemon -> {
//            scriptBuilder.append("(").append(pokemon.getPokedexID()+",")
//                    .append(TypesEnum.fromString(pokemon.getType()).getId()+",")
//                    .append("'"+pokemon.getName()+"'"+",")
//                    .append(pokemon.getPower())
//                    .append("), \n");
//        });
//        File newFile = new File("D:/download/insertPokemonMoves.sql");
//        FileWriter write = new FileWriter(newFile);
//        write.write(scriptBuilder.toString());
//        write.close();
//    }
    @GetMapping(value = {"*/*.html","*.html", "*/","/*","*/*"})
    public ResponseEntity<HttpStatus> test() throws IOException {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    private void sendDataToFrontEnd(String exchangeId, int code){
        List<SseEmitter> usedEmitter = new ArrayList<>();
        serverEmitters.forEach(sseEmitter -> {
                    try {
                        sseEmitter.send(SseEmitter.event()
                                .data(new PackageFrontEnd(exchangeId,code))
                                .id("exchange")
                                .name("pokemon"));
                        usedEmitter.add(sseEmitter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        usedEmitter.forEach(ResponseBodyEmitter::complete);
    }
}
