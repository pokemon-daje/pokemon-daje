let pokemons = [];

var source = new EventSource(
  "http://localhost:8080/api/pokemon/exchange/events"
);
source.addEventListener("pokemon", (event) => {
  console.log(JSON.parse(event.data));
});

var getPokemons = () => fetch("http://localhost:8080/api/pokemon").then((data) => {
  if (data.ok) {
    data.json().then((response) => {
        console.log(response);
      let carouselContainer = document.querySelector(".carousel-container");
      carouselContainer.innerHTML="";
      pokemons = response;
        for (let snglPokemon of response) {
          let card = document.createElement("li");
          card.setAttribute("class","card");

          addImageToCard(card,snglPokemon);
          addTitleToCard(card,snglPokemon);
          addTrainerToCard(card,snglPokemon);
          addModalButtonOpen(card,snglPokemon);
          carouselContainer.appendChild(card);
          addModalEvent(snglPokemon);
        }
      });
  }
});

function addModalEvent(snglPokemon){
    let idButton=`#button${snglPokemon.database_id}`;
    document.querySelector(idButton).addEventListener('click', function (event) {
        let modal = document.querySelector("#modalPokemon");
        modal.style.visibility = 'visible'
        modal.style.opacity = '0.5'
        modal.style.transform = 'scale(0.8)'
        let poke = pokemons.find((poke) => poke.database_id === Number.parseInt(event.target.id));
        modal.querySelector(".head-modal").innerHTML = poke.name;
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