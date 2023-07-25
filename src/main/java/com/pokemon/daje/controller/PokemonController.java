package com.pokemon.daje.controller;

import com.pokemon.daje.controller.json.dto.*;
import com.pokemon.daje.model.Pokemon;
import com.pokemon.daje.model.ProgressingProcessCode;

import com.pokemon.daje.service.PokemonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @Operation(summary = "Takes six Pokemon from the database randomly")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "500", description = "internal server error"),
    })
    @GetMapping("/pokemon")
    public ResponseEntity<List<PokemonFrontEndDTO>> getSixRandom() {
        List<PokemonFrontEndDTO> pokemonDTOList = pokemonService.getSixRandomPokemon();
        if(!pokemonDTOList.isEmpty()){
            return new ResponseEntity<>(pokemonDTOList,HttpStatus.OK);
        }
        return new ResponseEntity<>(pokemonDTOList,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Operation(summary = "Get Pokemon by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied"),
    })
    @GetMapping("/pokemon/{id}")
    public ResponseEntity<PokemonFrontEndDTO> getById(@Parameter(description = " id of the pokemon to be searched ")@PathVariable int id) {
        PokemonFrontEndDTO pokemonFrontEndDTO = pokemonService.getById(id);
        if(pokemonFrontEndDTO != null){
            return new ResponseEntity<>(pokemonFrontEndDTO,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @Operation(summary = "Create a new Pokemon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "400", description = "bad request"),
    })
    @PostMapping("/pokemon")
    public ResponseEntity<PokemonFrontEndDTO> insert(@RequestBody(description = " - id pokemon 1 to 151 NOT NULL\n" +
            " - id type 1 to 18 NOT NULL \n " +
            " - id move 1 to 161 NOT NULL \n" + "- id type moves 1 to 4  NOT NULL", required = true,
            content = @Content(
                    schema=@Schema(implementation = Pokemon.class)))Pokemon pokemon) {
        PokemonFrontEndDTO pokemonFrontEnd = pokemonService.insertFromFrontEnd(pokemon);
        if(pokemonFrontEnd != null){
            return new ResponseEntity<>(pokemonFrontEnd,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Concludes exchange action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok"),
            @ApiResponse(responseCode = "400", description = "bad request"),
    })
    @PostMapping("/pokemon/exchange")
    public ResponseEntity<PackageExchange> swap(@RequestBody(description = " - id pokemon 1 to 151 NOT NULL \n" +
            " - id type 1 to 18 NOT NULL \n" +
            " - id move 1 to 161 NOT NULL\n" + " - id type moves 1 to 4  NOT NULL", required = true,
            content = @Content(
                    schema=@Schema(implementation = PokemonExchangeDTO.class))) PokemonExchangeDTO pokemon) {
        PackageExchange pack = pokemonService.inizializePokemonsSwap(pokemon);
        ResponseEntity<PackageExchange> toSend = new ResponseEntity<>(pack,HttpStatus.OK);
        if(pack == null){
            sendDataToFrontEnd("exchange error",400);
            toSend = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return toSend;
    }
    @Operation(summary = "This method is not allowed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "405", description = "method not allowed"),
    })
    @GetMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorGet(@RequestBody PokemonExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @Operation(summary = "This method is not allowed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "405", description = "method not allowed"),
    })
    @PutMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorPut(@RequestBody PokemonExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @Operation(summary = "This method is not allowed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "405", description = "method not allowed"),
    })
    @DeleteMapping("/pokemon/exchange")
    public ResponseEntity<HttpStatus> swapErrorDelete(@RequestBody PokemonExchangeDTO pokemon) {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
    @Operation(summary = "Update the status of a Pokemon exchanged")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok",content = @Content),
            @ApiResponse(responseCode = "400", description = "bad request"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "405", description = "method not allowed"),
    })
    @PostMapping("/pokemon/exchange/{exchangeId}/status")
    public ResponseEntity<HttpStatus> statusSwap(@Parameter(description = " id of the pokemon to be exchanged ")@PathVariable("exchangeId") String exchangeId, @RequestBody (description = " - id pokemon 1 to 151 NOT NULL \n" +
            " - id type 1 to 18 NOT NULL \n" +
            " - id move 1 to 161 NOT NULL \n" + " - id type moves 1 to 4  NOT NULL", required = true,
            content = @Content(
                    schema=@Schema(implementation = PackageExchangeStatus.class))) PackageExchangeStatus packageExchangeStatus){
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

    @Operation(summary = "This method returns an SseEmitter that can be used to stream events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok",content = @Content),
            @ApiResponse(responseCode = "500", description = "internal serve error")
    })
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

    /*
    @Operation(summary = "This method gets a package pokemon.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ok",content = @Content),
            @ApiResponse(responseCode = "500", description = "internal serve error")
    })
      @PostMapping("/pokemon/getpackage")
      public void getPackagePokemons(@RequestBody List<GatherDataPokemonSpecie> pokemonExchangeDTOList) throws IOException {
        int ok =0;
     StringBuilder scriptBuilder = new StringBuilder().append("insert into pokemon_species(pokedex_id,type_id,name,sprite_url) values \n");
      pokemonExchangeDTOList.forEach(pokemon -> {
            scriptBuilder.append("(").append(pokemon.getId()+",")
                    .append(TypesEnum.fromString(pokemon.getType()).getId()+",")
                    .append("'"+pokemon.getName()+"'"+",")
                    .append("'"+pokemon.getSprite()+"'")
                    .append("), \n");
        });
        File newFile = new File("D:/download/insertPokemonSpecies.sql");
        FileWriter write = new FileWriter(newFile);
          write.write(scriptBuilder.toString());
          write.close();
   }*/
//  @Operation(summary = "This method gets a package of moves.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "ok",content = @Content),
//            @ApiResponse(responseCode = "500", description = "internal serve error")
//    })
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

    @Operation(summary = "This method returns the request not accept.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "bad request"),
    })
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
