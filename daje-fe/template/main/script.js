let wrapper = null;
let arrowBtns = null;
let carouselChildrens = null;
let counter = 0;
let firstCardOffSetWidth = 0;
let curtainsAreOpen= false;

function gatherData(){
    wrapper = document.querySelector(".wrapper");
    arrowBtns = document.querySelectorAll(".wrapper i");
    carouselChildrens = [...document.querySelectorAll(".carousel-container li")];
    counter = 0;
    firstCardOffSetWidth = carouselChildrens[0].offsetWidth;
}
function gatherDataLoading(){
    wrapper = document.querySelector(".wrapper");
    arrowBtns = document.querySelectorAll(".wrapper i");
    carouselChildrens = [...document.querySelectorAll(".carousel-container li")];
    counter = 0;
    firstCardOffSetWidth = carouselChildrens[0].offsetWidth;
    for(cardPoke of carouselChildrens){
        let pokemonOfCard = modifiedPokemons.find(poke => poke.database_id == cardPoke.id);
        let posPx = pokemonOfCard.pos*firstCardOffSetWidth + firstCardOffSetWidth/2 * pokemonOfCard.pos;

        pokemonOfCard.originalPos = posPx;
        cardPoke.style.transform = `translate(${posPx}px)`
        cardPoke.style.zIndex = "1";
    }
}
function nextImg(integer){
    console.log("prova")
    if(wrapper === null){
        gatherData()
    }
    halfScreen= window.screen.width/2
    carouselChildrens = [...document.querySelectorAll(".carousel-container li")];
    firstCardOffSetWidth = carouselChildrens[0].offsetWidth;
    if(integer > 0){
        counter++;
    }
    else{
        counter--;
    }
    if(counter >= carouselChildrens.length){
        counter = 0;
    }else if(counter <= -1){
        counter = carouselChildrens.length-1;
    }
    for(i=0;i<carouselChildrens.length;i++){
        animation(i);
    }
}

function animation(rootCard){
    let htmlCardPokemon = carouselChildrens[rootCard];
    let pokemonOfCard = modifiedPokemons.find(poke => poke.database_id == htmlCardPokemon.id);
    carouselChildrens[rootCard].style.transform = `translate(${-2*(counter*firstCardOffSetWidth)+pokemonOfCard.originalPos+(firstCardOffSetWidth/2*pokemonOfCard.pos)}px)`
}

function openCurtains(){
    if(!curtainsAreOpen){
        let curtainLeft = document.querySelector(".curtain-carousel-left");
        let curtainRight = document.querySelector(".curtain-carousel-right");
        let buttonLeft = document.querySelector("#left");
        let buttonRight = document.querySelector("#right");
        let buttonDiv = document.querySelector(".buttons");
        buttonDiv.style.opacity = '1';
        curtainLeft.style.transform = `translate(${-60}%)`;
        curtainRight.style.transform = `translate(${+60}%)`;
        buttonLeft.style.transform = 'rotate(360deg)';
        buttonRight.style.transform = 'rotate(-360deg)';
        getPokemons();
        curtainsAreOpen = true
    }
}
let startpos = ()=>{
    gatherData();
    for(i=0;i<carouselChildrens.length;i++){
        animation(i);
    }
}
startpos()