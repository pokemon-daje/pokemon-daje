let pokemons = [];
let modifiedPokemons = [];

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
    let idButton=`#button${snglPokemon.database_id}`;
    document.querySelector(idButton).addEventListener('click', function (event) {
        let modal = document.querySelector("#modalPokemon");
        modal.style.visibility = 'visible'
        modal.style.opacity = '1'
        modal.style.transform = 'scale(0.8)'
        let poke = pokemons.find((poke) => ("button"+poke.database_id) === event.target.id);
        console.log(poke);
        modal.querySelector("#current-hp").innerHTML =`<h5> CURRENT HP: ${ poke.current_hp} </h5>`;
        modal.querySelector("#max-hp").innerHTML =`<h5> MAX HP: ${ poke.max_hp}</h5>`;
      
        modal.querySelector("#type").innerHTML = `<p>TYPE NAME: ${ poke.type.name}   <img src="${poke.type.imageUrl}"></p>`;
        const movesContainer = modal.querySelector("#moves-name");
        poke.moves.forEach(move => {
          movesContainer.innerHTML += `<h5> MOVE NAME:${ move.name}   </h5>`;
        });
         const movesContainerPower = modal.querySelector("#moves-power");
         poke.moves.forEach(move => {
        movesContainerPower.innerHTML += `<h5> POWER: ${ move.power}</h5>`;
        });
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


function setColorByType(poke) {
  const card = document.getElementsByClassName('card');
  let backgroundColor;
  let typeId = poke.type.id;
  let coloPalette = {
    1: "#ffffff",
    2: 'rgba(255,0,0,1.0)',
    3: '#f400a1',
    4: '#f400a1',

  }

  return coloPalette[typeId];
  /*
  switch (typeId) {
    case 1: 
      backgroundColor = '#ffffff'; 
      break;
    case 2: 
      backgroundColor = 'rgba(255,0,0,1.0)'; 
      break;
      case 3: 
      backgroundColor = '#f400a1'; 
      break;
      case 4: 
      backgroundColor = '#f400a1'; 
      break;
      case 5: 
      backgroundColor = '#ff0000'; 
      break;
      case 6: 
      backgroundColor = '#ff0000'; 
      break;
      case 7: 
      backgroundColor = '#ff0000'; 
      break;
      case 8: 
      backgroundColor = '#ff0000'; 
      break;
      case 9: 
      backgroundColor = '#ff0000'; 
      break;
      case 10: 
      backgroundColor = '#ff0000'; 
      break;
      case 11: 
      backgroundColor = '#ff0000'; 
      break;
      case 12: 
      backgroundColor = '#ff0000'; 
      break;

    default:
      backgroundColor = '#FFFFFF'; 
      break;
  }*/

  card.style.backgroundColor = backgroundColor;
}