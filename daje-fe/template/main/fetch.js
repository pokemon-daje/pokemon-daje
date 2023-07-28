let pokemons = [];
let modifiedPokemons = [];
let pokemonSwaps = [];
let modalOpen = false;
let colorPalette = {
    1: "rgba(188,230,230)",
    2: 'rgba(134,98,143,0.8)',
    3: 'rgb(255, 228, 225)',
    4: 'rgb(255, 100, 0)',
    5: 'rgb(255, 203, 5)',
    6: 'rgb(176,119,68)',
    7: 'rgb(192, 192, 192)',
    8: 'rgb(255, 0, 0)',
    9: 'rgb(139, 115, 85)',
    10: 'rgb(138, 43, 226)',
    11: 'rgb(75,73,73)',
    12: 'rgb(255, 100, 0)',
    13: 'rgb(165,185,67)',
    14: 'rgb(232,125,204)',
    15: 'rgb(255, 192, 203)',
    16: 'rgb(100, 180, 100)',
    17: 'rgb(239, 235, 222)',
    18: 'rgb(0, 191, 255)',
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
    1: "https://vignette3.wikia.nocookie.net/legendarymob/images/c/ca/Ice_cave.jpg/revision/latest?cb=20160924215941",
    2: 'https://media.pokemoncentral.it/wiki/c/c0/Lavandonia_PO.png',
    3: 'https://img.freepik.com/free-vector/pastel-sky-background_23-2148900230.jpg?w=740&t=st=1690533400~exp=1690534000~hmac=ffcf5dfed6b1518be146d3a5ad2ea6da45074834dc1071416c0223998dfa4aa8',
    4: 'https://i.pinimg.com/564x/ec/c4/09/ecc4094b6774d8ec8614b9d328d2e3da.jpg',
    5: 'https://img.freepik.com/free-vector/flat-design-yellow-comics-wallpaper_23-2148801759.jpg?w=740&t=st=1690533524~exp=1690534124~hmac=19f81ad5796273f73304e17f7dc94e235f386d5941365f8b78770e20f79c6c34',
    6: 'https://i.pinimg.com/564x/b6/57/e9/b657e987391a50599b0ebb90d8dd63a9.jpg',
    7: 'https://i.pinimg.com/564x/ef/c7/45/efc745a9ddb6d81e77652d9667668c3e.jpg',
    8: 'https://cutewallpaper.org/21/anime-gym-background/Anime-Gymnasium-Background-Related-Keywords-Suggestions-.png',
    9: 'https://pbs.twimg.com/tweet_video_thumb/FSqD_jZXsAANef0.jpg',
    10: 'https://www.gameovercancer.ca/backgrounds/Poison.png',
    11: 'https://cdn.discordapp.com/attachments/1130423419368915076/1134411282821414972/bb4819ec39296e0b75b01ab6d2a1f110.png',
    12: 'https://cutewallpaper.org/21x/pe8g5cl1q/Best-29-Fire-Background-1080P-on-HipWallpaper-Terrifying-.jpg',
    13: 'https://cdn.openart.ai/stable_diffusion/c9947e97e2cb45211d19295a058c73b6136d33ce_2000x2000.webp',
    14: 'https://media.pokemoncentral.it/wiki/thumb/1/19/Laboratorio_Cadente_MDDX.png/1200px-Laboratorio_Cadente_MDDX.png',
    15: 'https://img.freepik.com/free-photo/sunset-sky-mountain-orange-yellow-blue-generative-ai_188544-9180.jpg?w=740&t=st=1690534228~exp=1690534828~hmac=070458ba391e1e619dfb1988a0a66e60a746b5b8e5620ced69f3ab623c160d2f',
    16: 'https://wallpapercave.com/wp/wp10311654.png',
    17: 'https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/5bbf58a6-1e85-4cfe-83fd-02df6f482b45/de8nlib-6bed7b3d-3d7e-4763-bb60-18f5ee0127fd.png/v1/fill/w_1024,h_652,q_80,strp/background_prairie_pokemon_screencapture_by_nemotrex_de8nlib-fullview.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9NjUyIiwicGF0aCI6IlwvZlwvNWJiZjU4YTYtMWU4NS00Y2ZlLTgzZmQtMDJkZjZmNDgyYjQ1XC9kZThubGliLTZiZWQ3YjNkLTNkN2UtNDc2My1iYjYwLTE4ZjVlZTAxMjdmZC5wbmciLCJ3aWR0aCI6Ijw9MTAyNCJ9XV0sImF1ZCI6WyJ1cm46c2VydmljZTppbWFnZS5vcGVyYXRpb25zIl19.ZVgNGi61AOkC068E-wphWmAUW8amj0MiJySqDcMCVh8',
    18: 'https://wallpapercave.com/wp/wp2690555.jpg',
    30000: 'https://wallpapers.com/images/featured/void-background-5sm9tokk2youui90.jpg'
};
setInterval(()=>{
    let swap = pokemonSwaps.shift();
    if(swap !== null && swap !== undefined){
        startUIUpdateDoToSwap(swap);
    }
},4000)

let uuid = crypto.randomUUID();
var source = new EventSource("http://localhost:8080/api/pokemon/exchange/events/"+uuid);
source.addEventListener("pokemon", (event) => {
    let swapEvent = JSON.parse(event.data);
    console.log(swapEvent);
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
    modal.style.transform = 'scale(0.8)';
    modal.style.boxShadow = `0em 1em 3em ${snglPokemon.type.id}`;
    let poke = pokemons.find((poke) => ("button" + poke.database_id) === event.target.id);
    resetModalGenerealInfo(modal,snglPokemon)
    populateMovesTable(snglPokemon)
  });
}
function populateMovesTable(poke){
    let table = document.getElementById("moves-table")
    table.style.boxShadow = `inset 0em 1em 3em ${colorPaletteDarken[poke.type.id]}`

    let tableBody = document.getElementById("moves-table-body");
    tableBody.innerHTML = "";
    poke.moves.forEach(move => {
        let trElement =document.createElement("tr")
        trElement.style.backgroundColor = `${colorPalette[move.type.id]}`
        trElement.style.border = `${colorPaletteDarken[move.type.id]} 5px solid`
        trElement.style.boxShadow = `inset 0em 1em 3em ${colorPaletteDarken[poke.type.id]}`

        let tr= `<td>${move.name}</td><td>${move.type?.name}</td><td>${move.power}</td>`;
        trElement.innerHTML = tr;

        tableBody.append(trElement);
    });
    document.querySelector('#close').addEventListener('click', function() {
        let modal = document.querySelector('#modalPokemon');
        modal.style.display = 'none';
        modalOpen = false;
    });
}
function resetModalGenerealInfo(modal,poke){
    document.getElementById("general-info").innerHTML ="<h5 id=\"current-hp\"></h5>\n"
        +"<h5 id=\"max-hp\"></h5>\n"
        +"<p id=\"type\"></p>";
    setPaletteColorModal(modal,poke)

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
    modal.style.display = modalOpen ? 'none' : 'block';
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







