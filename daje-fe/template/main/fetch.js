let pokemons = [];
let modifiedPokemons = [];
let pokemonSwapsConclude = [];
let pokemonInitializedSwaps = [];
let droppedSwaps = [];
let modalOpen = false;
let notifyTradeBeingShown = false;
let colorPalette = {
    1: "rgba(188,230,230)",
    2: 'rgba(134,98,143,0.8)',
    3: 'rgb(255, 228, 225)',
    4: 'rgb(166, 16, 34)',
    5: 'rgb(255, 203, 5)',
    6: 'rgb(176,119,68)',
    7: 'rgb(192, 192, 192)',
    8: 'rgb(203, 50, 52)',
    9: 'rgb(139, 115, 85)',
    10: 'rgb(134, 115, 161)',
    11: 'rgb(75,73,73)',
    12: 'rgb(236, 124, 38)',
    13: 'rgb(165,185,67)',
    14: 'rgb(232,125,204)',
    15: 'rgb(255, 192, 203)',
    16: 'rgb(100, 180, 100)',
    17: 'rgb(239, 235, 222)',
    18: 'rgb(109, 177, 216)',
    30000: 'rgb(96,70,70)'
};
let colorPaletteDarken = {
    1: "rgb(105,128,128)",
    2: 'rgba(86,64,94,0.8)',
    3: 'rgb(110,96,96)',
    4: 'rgb(155,58,2)',
    5: 'rgb(143,117,4)',
    6: 'rgb(128,88,49)',
    7: 'rgb(147,145,145)',
    8: 'rgb(161,1,1)',
    9: 'rgb(114,97,68)',
    10: 'rgb(86,27,140)',
    11: 'rgb(52,50,50)',
    12: 'rgb(128,52,2)',
    13: 'rgb(103,114,42)',
    14: 'rgb(134,73,119)',
    15: 'rgb(143,110,115)',
    16: 'rgb(54,98,54)',
    17: 'rgb(143,141,134)',
    18: 'rgb(3,99,129)',
    30000: 'rgb(65,47,47)'
};
let backgroundImage = {
    1: "/background_ice",
    2: '/background_ghost',
    3: '/background_fairy',
    4: '/background_dragon',
    5: '/background_electric',
    6: '/background_rock',
    7: '/background_steel',
    8: '/background_fighting',
    9: '/background_ground',
    10: '/background_poison',
    11: '/background_dark',
    12: '/background_fire',
    13: '/background_bug',
    14: '/background_psychic',
    15: '/background_flying',
    16: '/background_grass',
    17: '/background_normal',
    18: '/background_water',
    30000: '/background_unknown'
};
setInterval(()=>{
    if(pokemonInitializedSwaps.length >= 1 && !notifyTradeBeingShown){
        notifyTradeBeingShown = true;
        let initializedSwap = pokemonInitializedSwaps.shift()
        startSwapsNotifyModal(initializedSwap)
        setTimeout(()=>{
            let indexConcludedSwap = pokemonSwapsConclude.findIndex(swap => swap.exchange_id === initializedSwap.exchange_id);
            let concludedSwap = null;
            if( indexConcludedSwap > -1){
                concludedSwap = pokemonSwapsConclude[indexConcludedSwap]
                pokemonSwapsConclude = pokemonSwapsConclude.filter(swaps => swaps.exchange_id !== concludedSwap.exchange_id)
            }
            if(concludedSwap!= null
                && concludedSwap != undefined
                && concludedSwap.status_request_code === 200
                && concludedSwap.status_response_code === 200){
                startSwapsNotifyModal(concludedSwap)
                document.getElementById("notify-swap-modal").style.animation = 'success-swap 2s linear 1'
                swapPokemonsNotifyPosition(concludedSwap.pokemon_sent,concludedSwap.pokemon_receive);

                setTimeout(()=>{
                    resetNotifyModalAnimations()
                    startUIUpdateDoToSwap(concludedSwap);
                    notifyTradeBeingShown = false;
                },2300)
            }else if(concludedSwap == null || concludedSwap == undefined){
                startSwapsNotifyModal({...initializedSwap,status_response_code:408})
                document.getElementById("notify-swap-modal").style.animation = 'forfait-swap 2s linear 1'

                setTimeout(()=>{
                    resetNotifyModalAnimations()
                    notifyTradeBeingShown = false;
                },2300)
            }else{
                startSwapsNotifyModal({...initializedSwap,status_response_code:400})
                document.getElementById("notify-swap-modal").style.animation = 'error-swap 2s linear 1'

                setTimeout(()=>{
                    resetNotifyModalAnimations()
                    notifyTradeBeingShown = false;
                },2300)
            }
        },10000)
    }
},4000)

let uuid = crypto.randomUUID();
var source = new EventSource("http://localhost:8080/api/pokemon/exchange/events/"+uuid);
source.addEventListener("pokemon", (event) => {
    let swapEvent = JSON.parse(event.data);
    console.log(swapEvent)
    manageSwap(swapEvent);
});

function manageSwap(swap){
    if(swap != null
        && pokemons.length > 0
        && modifiedPokemons.length > 0){
        switch (swap.status_request_code){
            case 0: { // inizio scambio
                newSwap(swap)
                break;
            }
            default:{
                nextPhaseSwap(swap)
            }
        }
    }
}

function newSwap(swap){
    switch (swap.status_response_code){
        case 200: {
            pokemonInitializedSwaps.push(swap)
            break;
        }default: {
            droppedSwaps.push(swap);
        }
    }
}

function nextPhaseSwap(swap){
    pokemonSwapsConclude.push(swap);
}

function completeSwap(swap){
    switch (swap.status_response_code){
        case 200: {
            if (swap.pokemon_receive != null
                && swap.pokemon_sent != null
            ) {
                pokemonSwapsConclude.push({...swap,time:Date.now()});
            }
        }
    }
}

function startUIUpdateDoToSwap(swap){
    let snglCardPokemon = document.getElementById(swap.pokemon_sent.database_id)
    changeDataOnJollyCard(swap.pokemon_sent)
    if(snglCardPokemon){
        let listModPokePos = configureDataForCard(swap);
        animateOnSwap(swap,snglCardPokemon,listModPokePos);
        updateCardStructure(snglCardPokemon, swap.pokemon_receive);
    }
}
function configureDataForCard(swap){
    let listPokePos = pokemons.findIndex(poke => poke.database_id === swap.pokemon_sent.database_id)
    pokemons[listPokePos] = swap.pokemon_receive;

    let listModPokePos = modifiedPokemons.findIndex(poke => poke.database_id === swap.pokemon_sent.database_id)
    let posInScreen = modifiedPokemons[listModPokePos].pos;
    let posPxInScreen = modifiedPokemons[listModPokePos].originalPos;

    modifiedPokemons[listModPokePos] = {
        ...swap.pokemon_receive,
        pos: posInScreen,
        originalPos: posPxInScreen
    };
    return listModPokePos;
}

function changeDataOnJollyCard(snglPokemonToDelete){
    let jolly = document.getElementById("JOLLY")
    jolly.innerHTML = ""

    addImageToCard(jolly,snglPokemonToDelete);
    addTitleToCard(jolly,snglPokemonToDelete);
    addTrainerToCard(jolly,snglPokemonToDelete);
}
function animateOnSwap(swap,cardPokemon,pokePos){
    cardPokemon.setAttribute("class","card");
    let id = `${swap.pokemon_receive.database_id}`;
    cardPokemon.setAttribute("id",id);
    moveCardWhenSwapHappens(modifiedPokemons[pokePos].pos)
}

var getPokemons = () => fetch("http://localhost:8080/api/pokemon").then((data) => {
  if (data.ok) {
    data.json().then((response) => {
      let carouselContainer = document.querySelector(".carousel-container");
      [...carouselContainer.children].forEach(child => {
          let classValue= child.getAttribute("class");
          if(classValue != "curtain-carousel-right"
          && classValue != "curtain-carousel-left"
          && child.id !== 'JOLLY'){
              carouselContainer.removeChild(child);
          }
      })
        modifiedPokemon = [];
        pokemons = response;
        let psIndex = 0;
        for (let snglPokemon of response) {
            if(snglPokemon != null){
                modifiedPokemons.push({...snglPokemon,pos:psIndex,originalPos:0});
                psIndex++;

                let card = document.createElement("li");
                createCardStructure(carouselContainer,card,snglPokemon);
            }
        }
        setTimeout(()=>{
             gatherDataLoading();
        },100)
      });
  }
});

function createCardStructure(carouselContainer,card,snglPokemon){
    card.setAttribute("class","card");
    let id = `${snglPokemon.database_id}`;
    card.setAttribute("id",id);

    addImageToCard(card,snglPokemon);
    addTitleToCard(card,snglPokemon);
    addTrainerToCard(card,snglPokemon);
    addModalButtonOpen(card,snglPokemon);
    carouselContainer.appendChild(card);
    addModalEvent(snglPokemon);
}

function updateCardStructure(card,snglPokemon){
    card.innerHTML = ""

    addImageToCard(card,snglPokemon);
    addTitleToCard(card,snglPokemon);
    addTrainerToCard(card,snglPokemon);
    addModalButtonOpen(card,snglPokemon);
    addModalEvent(snglPokemon);
}
function addModalEvent(snglPokemon){
  let idButton = `#button${snglPokemon.database_id}`;
  document.querySelector(idButton).addEventListener('click', function (event) {
    let modal = document.querySelector("#modalPokemon");
    modal.style.visibility = 'visible';
    modal.style.opacity = '1';
    modal.style.animation = 'modal-transition 1.7s linear 1';
    modal.style.boxShadow = `0em 1em 3em ${snglPokemon.type.id}`;
    modalOpen = true;
    resetModalGenerealInfo(modal,snglPokemon)
    populateMovesTable(snglPokemon)
  });
}
function populateMovesTable(poke){

    let movesBody = document.getElementById("moves");
        movesBody.style.boxShadow = `inset 0em 1em 3em ${colorPaletteDarken[poke.type.id]}`
        movesBody.innerHTML = "";
    poke.moves.forEach(move => {
        let trElement =document.createElement("tr")
        trElement.style.backgroundColor = `${colorPalette[move.type.id]}`
        trElement.style.margin = '2%'
        trElement.style.boxShadow = `inset 0em 1em 3em ${colorPaletteDarken[poke.type.id]}`

        let tr= `<div><span>${move.name}</span><img id="img${move.type.id}" src="${move.type.imageUrl}"><span id="move${move.type.id}" class="type-move">${move.type.name}</span></div><div><span>${move.power}</span></div>`;
        trElement.innerHTML = tr;
        movesBody.append(trElement);
        document.getElementById(`img${move.type.id}`).addEventListener("mouseover",()=>{
            document.getElementById(`move${move.type.id}`).style.visibility = 'visible'
        })
        document.getElementById(`img${move.type.id}`).addEventListener("mouseout",()=>{
            document.getElementById(`move${move.type.id}`).style.visibility = 'hidden'
        })
    });
    document.querySelector('#close').addEventListener('click', function() {
        let modal = document.querySelector('#modalPokemon');
        modal.style.visibility = 'hidden';
        modal.style.animation = '';
        modalOpen = false;
    });
}
function resetModalGenerealInfo(modal,poke){
    document.getElementById("general-info").innerHTML ="<h5 id=\"current-hp\"></h5>\n"
        +"<h5 id=\"max-hp\"></h5>\n"
        +"<p id=\"type\"></p>";
    setPaletteColorModal(modal,poke)
    modal.style.backgroundImage = `url(${poke.sprite_url})`
    modal.style.backgroundSize = '30%'
    modal.style.backgroundRepeat = 'no-repeat'
    modal.style.backgroundPositionY = '5%'
    modal.style.backgroundPositionX = '90%'

    document.getElementById("current-hp").innerHTML = `<h5>CURRENT HP: ${poke.current_hp} </h5>`;
    document.getElementById("max-hp").innerHTML = `<h5>MAX HP: ${poke.max_hp}</h5>`;
    document.getElementById("type").innerHTML = `<p>TYPE: <img src="${poke.type.imageUrl}">
        <span style="visibility: hidden">${poke.type.name}</span>
    </p>`;

    let typeIMG = document.getElementById("type").querySelector("p img")
    let typeName = document.getElementById("type").querySelector("p span")
    typeIMG.addEventListener("mouseover", ()=>{
        typeName.style.visibility = "visible";
    })
    typeIMG.addEventListener("mouseout", ()=>{
        typeName.style.visibility = "hidden";
    })
}
function setBackGroundCard(card,pokemon){
    card.style.backgroundColor = colorPalette[pokemon.type.id];
    card.style.backgroundImage = `url(${backgroundImage[pokemon.type.id]})`
    card.style.backgroundSize = `100% 50%`;
    card.style.backgroundRepeat = 'no-repeat';
}
function setPaletteColorModal(modal,poke){
    let typeId = poke.type.id;
    modal.style.backgroundColor = colorPalette[typeId];
    if (typeId === 11 || typeId === 30000 || typeId === 2)
        modal.style.color = 'white';
    else{
        modal.style.color = 'black';
    }
    modal.style.visible = modalOpen ? 'visible' : 'hidden';
}
function addImageToCard(card,snglPokemon){
    let imgContainer = document.createElement("div");
    imgContainer.setAttribute("class","img");

    let imgCard = document.createElement("img");
    imgCard.setAttribute("src",snglPokemon.sprite_url);
    imgContainer.appendChild(imgCard);
    card.appendChild(imgContainer);
    setBackGroundCard(card,snglPokemon);
}

function addTitleToCard(card,snglPokemon){
    let cardH2 = document.createElement("h2");
    cardH2.innerHTML = snglPokemon.pokedex_id+" - "+ snglPokemon.name;
    card.appendChild(cardH2);
    cardH2.style.fontSize= '1em';
    cardH2.style.fontWeight = 'bold';
}

function addTrainerToCard(card,snglPokemon){
    let cardSpan = document.createElement("span");
    cardSpan.innerHTML = snglPokemon.original_trainer;
    card.appendChild(cardSpan)
    cardSpan.style.fontSize='1em';
    cardSpan.style.fontWeight='bold';
}

function addModalButtonOpen(card,snglPokemon){
    let cardButton = document.createElement("button")
    cardButton.setAttribute("class","btn btn-primary");
    cardButton.setAttribute("id",`button${snglPokemon.database_id}`)
    cardButton.innerHTML="check info";
    card.appendChild(cardButton)
}

function startSwapsNotifyModal(swap){
    let notifyModal = document.getElementById("notify-swap-modal")
    notifyModal.style.visibility = "visible"
    notifyModal.style.animation = "send-in-progess 2s linear 5"
    let header = document.getElementById("notify-head")
    let action = getActionSwap(swap.status_request_code, swap.status_response_code)
    header.innerHTML = action;
    addPokemonsToNotifyModal(swap.pokemon_sent,swap.pokemon_receive)
}

function swapPokemonsNotifyPosition(pokemonOnExit,pokemonOnReceive){
    if(pokemonOnExit != null){
        let divPokemonOnExit = document.getElementById("pokemon-on-exit")
        divPokemonOnExit.innerHTML = "";
        divPokemonOnExit.innerHTML = `<img src="${pokemonOnExit.sprite_url}">`
        divPokemonOnExit.style.animation = 'send-pokemon-swap-complete 2s linear 1'
    }
    if(pokemonOnReceive != null){
        let divPokemonOnReceive = document.getElementById("pokemon-on-receive")
        divPokemonOnReceive.innerHTML = "";
        divPokemonOnReceive.innerHTML = `<img src="${pokemonOnReceive.sprite_url}">`
        divPokemonOnReceive.style.animation = 'receive-pokemon-swap-complete 2s linear 1'
    }
}

function addPokemonsToNotifyModal(pokemonOnExit,pokemonOnReceive){
    if(pokemonOnExit != null){
        let divPokemonOnExit = document.getElementById("pokemon-on-exit")
        divPokemonOnExit.innerHTML = "";
        divPokemonOnExit.innerHTML = `<img src="${pokemonOnExit.sprite_url}">`
        divPokemonOnExit.style.animation = 'send-pokemon-swap 6.8s linear 2'
    }
    if(pokemonOnReceive != null){
        let divPokemonOnReceive = document.getElementById("pokemon-on-receive")
        divPokemonOnReceive.innerHTML = "";
        divPokemonOnReceive.innerHTML = `<img src="${pokemonOnReceive.sprite_url}">`
        divPokemonOnReceive.style.animation = 'receive-pokemon-swap 6.8s linear 2'
    }
}
function resetNotifyModalAnimations(){
    document.getElementById("notify-swap-modal").style.visibility = 'hidden';
    document.getElementById("notify-swap-modal").style.animation = ''
    resetPokemonsNotifyDivAnimation()
}
function resetPokemonsNotifyDivAnimation(){
    let divPokemonOnExit = document.getElementById("pokemon-on-exit")
    let divPokemonOnReceive = document.getElementById("pokemon-on-receive")
    divPokemonOnExit.style.animation = '';
    divPokemonOnReceive.style.animation = '';
}

function getActionSwap(swapAction,swapresponse){
    switch (swapAction){
        case 0:{
            return getResponseActionInizialized(swapresponse);
        }
        case 200:{
            return getResponseActionConclude(swapresponse);
        }
        default:{
            return getResponseActionConclude(swapresponse)
        }
    }
}

function getResponseActionInizialized(swapResponse){
    switch (swapResponse){
        case 200:{
            return "SWAP HAS BEEN INITIALIZED"
        }
        case 408:{
            return "SWAP FORFAIT"
        }
        default:{
            return "SWAP FAILED"
        }
    }
}
function getResponseActionConclude(swapResponse){
    switch (swapResponse){
        case 200:{
            return "SWAP HAS BEEN SUCCESSFULLY COMPLETED"
        }
        default:{
            return "SWAP FAILED"
        }
    }
}





