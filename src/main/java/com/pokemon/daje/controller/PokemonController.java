package com.pokemon.daje.controller;

import com.pokemon.daje.model.api_objects.ConcludeSwapRequest;
import com.pokemon.daje.model.api_objects.FrontEndSendData;
import com.pokemon.daje.model.api_objects.PokemonFrontEndDTO;
import com.pokemon.daje.model.api_objects.PokemonRequestExchangeDTO;
import com.pokemon.daje.model.business_data.Pokemon;
import com.pokemon.daje.model.business_data.SwapBankAction;
import com.pokemon.daje.service.PokemonService;
import com.pokemon.daje.service.SwapScheduleService;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.PrintWriter;
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
    private SwapScheduleService swapScheduleService;

    @Autowired
    public PokemonController(PokemonService pokemonService, SwapScheduleService swapScheduleService) {
        this.pokemonService = pokemonService;
        this.swapScheduleService = swapScheduleService;
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
    public void initializeSwap(HttpServletRequest request, HttpServletResponse response, @RequestBody PokemonRequestExchangeDTO pokemon) {
        AsyncContext asyncContext = request.startAsync(request,response);
        if(pokemon != null
                && pokemon.getId() != null
                && pokemon.getMoves() != null
                && pokemon.getType() != null
                && !pokemon.getMoves().isEmpty()
                && pokemon.getCurrentHP() != null
                && pokemon.getMaxHP() != null
                && pokemon.getOriginalTrainer() != null
        ){
            pokemonService.addInitializeSwapRequest(asyncContext, pokemon);
        }
        else{
            responseBadRequest(asyncContext);
        }
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
    public void concludeSwap(@PathVariable("exchangeId") String exchangeId,
                             @RequestBody ConcludeSwapRequest concludeSwapRequest,
                             HttpServletRequest request,
                             HttpServletResponse response){
        AsyncContext asyncContext = request.startAsync(request,response);
        if(concludeSwapRequest.getStatus() != null){
            pokemonService.addConcludeSwapRequest(asyncContext, exchangeId, concludeSwapRequest);
        }else{
            responseBadRequest(asyncContext);
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
                                        .data(new FrontEndSendData(exchangeId, responseCode, requestCode, pokemonSent, pokemonReceive))
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

    private void responseBadRequest(AsyncContext asyncContext){
        try {
            PrintWriter out = asyncContext.getResponse().getWriter();
            HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            out.write("WHAT ARE YOU DOING");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
