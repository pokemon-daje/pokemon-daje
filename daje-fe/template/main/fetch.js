let pokemons = [];
let modifiedPokemons = [];
//variabile booleana imposta la x a false
let modalOpen = false;

var source = new EventSource(
  "http://localhost:8080/api/pokemon/exchange/events"
);
source.addEventListener("pokemon", (event) => {
  console.log(JSON.parse(event.data));
});

var getPokemons = () => fetch("http://localhost:8080/api/pokemon").then((data) => {
  if (data.ok) {
    data.json().then((response) => {
      let carouselContainer = document.querySelector(".carousel-container");
      [...carouselContainer.children].forEach(child => {
          let classValue= child.getAttribute("class");
          if(classValue != "curtain-carousel-right"
          && classValue != "curtain-carousel-left"
          && classValue != "band-left"
          && classValue != "band-right"){
              carouselContainer.removeChild(child);
          }
      })
        modifiedPokemon = [];
        pokemons = response;
        let psIndex = 0;
        for (let snglPokemon of response) {
          modifiedPokemons.push({...snglPokemon,pos:psIndex,originalPos:0})
          psIndex++;
          let card = document.createElement("li");
          card.setAttribute("class","card");
          let id = `${snglPokemon.database_id}`;
          card.setAttribute("id",id)

          addImageToCard(card,snglPokemon);
          addTitleToCard(card,snglPokemon);
          addTrainerToCard(card,snglPokemon);
          addModalButtonOpen(card,snglPokemon);
          carouselContainer.appendChild(card);
          addModalEvent(snglPokemon);
        }
        setTimeout(()=>{
             gatherDataLoading()
        },100)  
      });
      
  }
  
});

function addModalEvent(snglPokemon){
  let idButton = `#button${snglPokemon.database_id}`;
  document.querySelector(idButton).addEventListener('click', function (event) {
    let modal = document.querySelector("#modalPokemon");
    modal.style.visibility = 'visible';
    modal.style.opacity = '1';
    modal.style.transform = 'scale(0.8)';

    let poke = pokemons.find((poke) => ("button" + poke.database_id) === event.target.id);
    console.log(poke);

    modal.querySelector("#current-hp").innerHTML = `<h5> CURRENT HP:  ${poke.current_hp} </h5>`;
    modal.querySelector("#max-hp").innerHTML = `<h5> MAX HP:  ${poke.max_hp}</h5>`;

    modal.querySelector("#type").innerHTML = `<p>TYPE NAME:  ${poke.type.name}   <img src="${poke.type.imageUrl}"></p>`;
    const movesContainer = modal.querySelector("#moves-name");
    movesContainer.innerHTML = ""; // Rimuove il contenuto precedente prima di aggiungere le nuove mosse
    poke.moves.forEach(move => {
      movesContainer.innerHTML += `<h5> MOVE NAME:  ${move.name}   </h5>`;
    });

    const movesContainerPower = modal.querySelector("#moves-power");
    movesContainerPower.innerHTML = ""; // Rimuove il contenuto precedente prima di aggiungere i nuovi valori di potenza
    poke.moves.forEach(move => {
      movesContainerPower.innerHTML += `<h5> POWER: ${move.power}</h5>`;
    });
    let typeId = poke.type.id;
    let coloPalette = {
      1: "#BCE6E6",
      2: 'rgba(150,150,150,.8)',
      3: 'rgb(255, 228, 225)',
      4: 'rgb(255, 100, 0)',
      5: 'rgb(255, 203, 5)',
      6: 'rgb(128, 128, 128)',
      7: 'rgb(192, 192, 192)',
      8: 'rgb(255, 0, 0)',
      9: 'rgb(139, 115, 85)',
      10: 'rgb(138, 43, 226)',
      11: 'rgb(0, 0, 0)',
      12: 'rgb(255, 100, 0)',
      13: 'rgb(200, 200, 200)',
      14: 'rgb(234, 234, 224)',
      15: 'rgb(255, 192, 203)',
      16: 'rgb(100, 180, 100)',
      17: 'rgb(239, 235, 222)',
      18: 'rgb(0, 191, 255)'
    };
    modal.style.backgroundColor = coloPalette[typeId];
    if (typeId === 11) 
      modal.style.color = 'white';
//controllo modale aperta o chiusa
    modal.style.display = modalOpen ? 'none' : 'block';  
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

document.getElementById('close').addEventListener('click', function() {
  const modal = document.getElementById('modalPokemon');
  modal.style.display = 'none';
  modalOpen = false; // Imposta la variabile booleana a "false" per indicare che la modale è stata chiusa
});







