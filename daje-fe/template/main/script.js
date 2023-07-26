let wrapper = null;
let carousel = null;
let arrowBtns = null;
let carouselChildrens = null;
let counter = -1;
let firstCardOffSetWidth = 0;
let halfScreen= window.screen.width/2;
let carouselWidth = 0;
let cardsOffset = 0;
let frame= 0;
let curtainsAreOpen= false;

function gatherData(){
     wrapper = document.querySelector(".wrapper");
     carousel = document.querySelector(".carousel-container");
     arrowBtns = document.querySelectorAll(".wrapper i");
     carouselChildrens = [...carousel.children];
     counter = 0;
     firstCardOffSetWidth = carouselChildrens[0].offsetWidth;
    cardsOffset = carouselChildrens.length * firstCardOffSetWidth + firstCardOffSetWidth/2;
}
function nextImg(integer){
    if(wrapper === null){
        gatherData()
    }
    halfScreen= window.screen.width/2
    carouselWidth = carousel.clientWidth
    firstCardOffSetWidth = carouselChildrens[0].offsetWidth;
    cardsOffset = carouselChildrens.length * firstCardOffSetWidth/2 + firstCardOffSetWidth/2;
    if(integer > 0){
        counter++;
    }
    else{
        counter--;
    }
    if(counter > carouselChildrens.length){
        counter = 2;
    }else if(counter < 2){
        counter = carouselChildrens.length;
    }
    for(i=0;i<carouselChildrens.length;i++){
        animation(i);
    }
}

function animation(rootCard){
    carouselChildrens[rootCard].style.transform = `translate(${counter*(firstCardOffSetWidth)-firstCardOffSetWidth/2-cardsOffset}px)`
    carouselChildrens[i].style.zIndex = "1";
}

function openCurtains(){
    if(!curtainsAreOpen){
        let curtainLeft = document.querySelector(".curtain-carousel-left");
        let curtainRight = document.querySelector(".curtain-carousel-right");
        let buttonLeft = document.querySelector("#left");
        let buttonRight = document.querySelector("#right");
        let buttonDiv = document.querySelector(".buttons");
        buttonDiv.style.opacity = '1';
        curtainLeft.style.transform = `translate(${-70}%)`;
        curtainRight.style.transform = `translate(${+70}%)`;
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