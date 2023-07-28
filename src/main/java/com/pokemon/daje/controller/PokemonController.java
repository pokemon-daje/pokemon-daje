package com.pokemon.daje.controller;

import com.pokemon.daje.controller.json.dto.*;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.model.ProgressingProcessCode;
import com.pokemon.daje.model.SwapBankAction;
import com.pokemon.daje.service.PokemonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api")
public class PokemonController {
    final PokemonService pokemonService;
    private Map<String, SseEmitter> serverEmitters;
    ExecutorService sseMvcExecutor;

    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
        serverEmitters = new HashMap<>();
        sseMvcExecutor = Executors.newSingleThreadExecutor();
    }

    @GetMapping("/pokemon")
    public ResponseEntity<List<PokemonFrontEndDTO>> getSixRandom() {
        List<PokemonFrontEndDTO> pokemonDTOList = pokemonService.getSixRandomPokemon();
        if(!pokemonDTOList.isEmpty()){
            return new ResponseEntity<>(pokemonDTOList,HttpStatus.OK);
        }
        return new ResponseEntity<>(pokemonDTOList,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/pokemon/{id}")
    public ResponseEntity<PokemonFrontEndDTO> getById(@PathVariable int id) {
        PokemonFrontEndDTO pokemonFrontEndDTO = pokemonService.getById(id);
        if(pokemonFrontEndDTO != null){
            return new ResponseEntity<>(pokemonFrontEndDTO,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/pokemon")
    public ResponseEntity<PokemonFrontEndDTO> insert(@RequestBody Pokemon pokemon) {
        PokemonFrontEndDTO pokemonFrontEnd = pokemonService.insertFromFrontEnd(pokemon);
        if(pokemonFrontEnd != null){
            return new ResponseEntity<>(pokemonFrontEnd,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/pokemon/exchange")
    public ResponseEntity<PackageExchange> swap(@RequestBody PokemonExchangeDTO pokemon) {
        PackageExchange pack = pokemonService.inizializePokemonsSwap(pokemon);
        ResponseEntity<PackageExchange> toSend = new ResponseEntity<>(pack,HttpStatus.OK);
        if(pack == null){
            sentDataToFrontEnd("exchange error",ProgressingProcessCode.POKEMON_BAD_REQUEST.getCode()
                    ,ProgressingProcessCode.POKEMON_EXCHANGE_REQUEST_OPEN.getCode());
            toSend = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            sentDataToFrontEnd(pack.getId(),ProgressingProcessCode.POKEMON_REQUEST_SUCCESS.getCode()
                    ,ProgressingProcessCode.POKEMON_EXCHANGE_REQUEST_OPEN.getCode());
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
                && !ProgressingProcessCode.POKEMON_REQUEST_UNKWON.equals(ProgressingProcessCode.fromNumber(packageExchangeStatus.getStatus()))
        ){
            code = pokemonService.nextStepSwap(exchangeId,packageExchangeStatus);
            sentDataToFrontEnd(exchangeId,code.getCode(), packageExchangeStatus.getStatus());
        } else {
            code = ProgressingProcessCode.POKEMON_BAD_REQUEST;
        }

        switch (code){
            case POKEMON_REQUEST_SUCCESS -> {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            case POKEMON_BAD_REQUEST -> {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            case POKEMON_EXCHANGE_NOT_FOUND -> {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            default -> {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping("/pokemon/exchange/events/{eventId}")
    public SseEmitter streamSseMvc(@PathVariable String eventId) {
        SseEmitter emitter = new SseEmitter();
        sseMvcExecutor.execute(() -> {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data(new Date())
                        .id("connection")
                        .name("pokemon");
                emitter.send(event);
                serverEmitters.put(eventId, emitter);
                emitter.onTimeout(() -> serverEmitters.remove(eventId));

            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
    @GetMapping(value = {"*/*.html","*.html", "*/","/*","*/*"})
    public ResponseEntity<HttpStatus> test(){
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private void sendDataToFrontEnd(String exchangeId, int responseCode, int requestCode, PokemonFrontEndDTO pokemonSent, PokemonFrontEndDTO pokemonReceive) {
        sseMvcExecutor.execute(() -> {
            try {
                serverEmitters.forEach((key, sseEmitter) -> {
                            try {
                                sseEmitter.send(SseEmitter.event()
                                        .data(new PackageFrontEnd(exchangeId, responseCode, requestCode, pokemonSent, pokemonReceive))
                                        .id("exchange")
                                        .name("pokemon"));
                                sseEmitter.complete();
                            } catch (Exception ex) {
                                String copyKey = key + "";
                                sseEmitter.complete();
                                log.info("Connection with id: " + key + " has been closed");
                            }
                        }
                );
            } catch (Exception ex) {
                log.info("Server emitters gone crazy");
            }
        });
    }

    private void sentDataToFrontEnd(String exchangeId, int responseCode, int requestCode) {
        Map<SwapBankAction, PokemonFrontEndDTO> mapDeposit = pokemonService.getPokemonsFromSwapCacheLog(exchangeId);
        sendDataToFrontEnd(exchangeId, responseCode, requestCode,mapDeposit.get(SwapBankAction.TODELETE), mapDeposit.get(SwapBankAction.TOSAVE));
    }

}
