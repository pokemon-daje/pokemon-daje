let pokemons = [];
let modifiedPokemons = [];
let pokemonSwaps = [];
setInterval(()=>{
    let swap = pokemonSwaps.shift();
    if(swap !== null && swap !== undefined){
        startUIUpdateDoToSwap(swap);
    }
},4000)

let uuid = crypto.randomUUID();
var source = new EventSource("http://localhost:8080/api/pokemon/exchange/events/"+uuid);
source.addEventListener("pokemon", (event) => {
    console.log(JSON.parse(event.data))
    let swapEvent = JSON.parse(event.data);
    manageSwap(swapEvent);
});

function manageSwap(swap){
    if(swap != null
        && pokemons.length > 0
        && modifiedPokemons.length > 0){
        switch (swap.status_request_code){
            case 0: {
                newSwap(swap)
                break;
            }
            case 200: {
                nextPhaseSwap(swap)
                break;
            }
        }
    }
}

function newSwap(swap){
    switch (swap.status_response_code){
        case 200: {
            break;
        }
    }
}

function nextPhaseSwap(swap){
    switch (swap.status_request_code){
        case 200: {
            completeSwap(swap);
            break;
        }
    }
}

function completeSwap(swap){
    switch (swap.status_response_code){
        case 200: {
            if (swap.pokemon_receive != null
                && swap.pokemon_sent != null
            ) {
                pokemonSwaps.push({exchange_id:swap.exchange_id,time:Date.now(),pokemon_receive:swap.pokemon_receive,pokemon_sent:swap.pokemon_sent});
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
          modifiedPokemons.push({...snglPokemon,pos:psIndex,originalPos:0});
          psIndex++;

          let card = document.createElement("li");
          createCardStructure(carouselContainer,card,snglPokemon);
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
    let idButton=`#button${snglPokemon.database_id}`;
    document.querySelector(idButton).addEventListener('click', function (event) {
        let modal = document.querySelector("#modalPokemon");
        modal.style.visibility = 'visible'
        modal.style.opacity = '0.5'
        modal.style.transform = 'scale(0.8)'
        let poke = pokemons.find((poke) => ("button"+poke.database_id) === event.target.id);
        modal.querySelector("#head-modal").innerHTML = poke.name;
    });
}

function addImageToCard(card,snglPokemon){
    let imgContainer = document.createElement("div");
    imgContainer.setAttribute("class","img");

    let imgCard = document.createElement("img");
    imgCard.setAttribute("src",snglPokemon.sprite_url);
    imgContainer.appendChild(imgCard);
    card.appendChild(imgContainer);
}

function addTitleToCard(card,snglPokemon){
    let cardH2 = document.createElement("h2");
    cardH2.innerHTML = snglPokemon.pokedex_id+"| "+snglPokemon.name;
    card.appendChild(cardH2);
}

function addTrainerToCard(card,snglPokemon){
    let cardSpan = document.createElement("span");
    cardSpan.innerHTML = snglPokemon.original_trainer;
    card.appendChild(cardSpan)
}

function addModalButtonOpen(card,snglPokemon){
    let cardButton = document.createElement("button")
    cardButton.setAttribute("class","btn btn-primary");
    cardButton.setAttribute("id",`button${snglPokemon.database_id}`)
    cardButton.innerHTML="check info";
    card.appendChild(cardButton)
}