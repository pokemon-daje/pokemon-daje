package com.pokemon.daje.controller;

import com.pokemon.daje.model.api_objects.PackageExchangeStatus;
import com.pokemon.daje.model.api_objects.PackageFrontEnd;
import com.pokemon.daje.model.api_objects.PokemonFrontEndDTO;
import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.business_data.Pokemon;
import com.pokemon.daje.model.business_data.SwapBankAction;
import com.pokemon.daje.service.PokemonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private ExecutorService sseMvcExecutor;

    @Autowired
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
        serverEmitters = new HashMap<>();
        sseMvcExecutor = Executors.newSingleThreadExecutor();
    }

    @GetMapping("/pokemon")
    public ResponseEntity<List<PokemonFrontEndDTO>> getSixRandom() {
        List<PokemonFrontEndDTO> pokemonDTOList = pokemonService.getPokemonsFromStorage();
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
    public void swap(HttpServletRequest request,HttpServletResponse response, @RequestBody PokemonRequestExchangeDTO pokemon) {
        pokemonService.addInizalizeExchangeRequest(request.startAsync(request,response), pokemon);
    }
    @GetMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorGet(@RequestBody PokemonRequestExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @PutMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorPut(@RequestBody PokemonRequestExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @DeleteMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorDelete(@RequestBody PokemonRequestExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping("/pokemon/exchange/{exchangeId}/status")
    public void statusSwap(@PathVariable("exchangeId") String exchangeId,
                           @RequestBody PackageExchangeStatus packageExchangeStatus,
                           HttpServletRequest request,
                           HttpServletResponse response){
        pokemonService.concludeExchangeRequest(request.startAsync(request,response), exchangeId,packageExchangeStatus);
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

    private void sendDataToFrontEnd(String exchangeId, int responseCode, int requestCode) {
        Map<SwapBankAction, PokemonFrontEndDTO> mapDeposit = pokemonService.getPokemonsFromSwapCacheLog(exchangeId);
        sendDataToFrontEnd(exchangeId, responseCode, requestCode,mapDeposit.get(SwapBankAction.TODELETE), mapDeposit.get(SwapBankAction.TOSAVE));
    }

}
