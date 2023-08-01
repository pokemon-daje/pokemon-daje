let http = require('http');
let fs = require('fs');
let url = require('url');
let jsScript = '';
let fetchScript = '';
let routesScript = '';
let indexHTML = '';
let carillionHTML = '';
let styleCss = '';
let resetCss = '';
let rightArrow = '';
let leftArrow = ''
let leftPokemonCurtain ='';
let rightPokemonCurtain ='';
let backgroundLogoPokemons = '';
let backgrounfImage ="";
let backgroundIce = "";
let backgroundGhost = "";
let backgroundFairy = "";
let backgroundDragon = "";
let backgroundElectric = "";
let backgroundRock = "";
let backgroundSteel = "";
let backgroundFighting = "";
let backgroundGround = "";
let backgroundPoison = "";
let backgroundDark = "";
let backgroundFire = "";
let backgroundBug = "";
let backgroundPsychic = "";
let backgroundFlying = "";
let backgroundGrass = "";
let backgroundNormal = "";
let backgroundWater = "";
let backgroundUnknown = "";

let typeIce = "";
let typeGhost = "";
let typeFairy = "";
let typeDragon = "";
let typeElectric = "";
let typeRock = "";
let typeSteel = "";
let typeFighting = "";
let typeGround = "";
let typePoison = "";
let typeDark = "";
let typeFire = "";
let typeBug = "";
let typePsychic = "";
let typeFlying = "";
let typeGrass = "";
let typeNormal = "";
let typeWater = "";
let typeUnknown = "";
fs.readFile("./template/main/images/type/type_unknown.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeUnknown = type;
});
fs.readFile("./template/main/images/type/type_water.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeWater = type;
});
fs.readFile("./template/main/images/type/type_normal.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeNormal = type;
});
fs.readFile("./template/main/images/type/type_grass.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeGrass = type;
});
fs.readFile("./template/main/images/type/type_flying.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeFlying = type;
});
fs.readFile("./template/main/images/type/type_psychic.png", function (err, type) {
    if (err) {
        throw err;
    }
    typePsychic = type;
});
fs.readFile("./template/main/images/type/type_bug.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeBug = type;
});
fs.readFile("./template/main/images/type/type_fire.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeFire = type;
});
fs.readFile("./template/main/images/type/type_dark.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeDark = type;
});
fs.readFile("./template/main/images/type/type_poison.png", function (err, type) {
    if (err) {
        throw err;
    }
    typePoison = type;
});
fs.readFile("./template/main/images/type/type_ground.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeGround = type;
});
fs.readFile("./template/main/images/type/type_fighting.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeFighting = type;
});
fs.readFile("./template/main/images/type/type_steel.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeSteel = type;
});
fs.readFile("./template/main/images/type/type_rock.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeRock = type;
});
fs.readFile("./template/main/images/type/type_electric.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeElectric = type;
});
fs.readFile("./template/main/images/type/type_dragon.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeDragon = type;
});
fs.readFile("./template/main/images/type/type_fairy.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeFairy = type;
});
fs.readFile("./template/main/images/type/type_ghost.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeGhost = type;
});
fs.readFile("./template/main/images/type/type_ice.png", function (err, type) {
    if (err) {
        throw err;
    }
    typeIce = type;
});

fs.readFile("./template/main/images/background/background_unknown.jpg", function (err, backUnknown) {
    if (err) {
        throw err;
    }
    backgroundUnknown = backUnknown;
});
fs.readFile("./template/main/images/background/background_water.jpg", function (err, backWater) {
    if (err) {
        throw err;
    }
    backgroundWater = backWater;
});
fs.readFile("./template/main/images/background/background_normal.jpg", function (err, backNormal) {
    if (err) {
        throw err;
    }
    backgroundNormal = backNormal;
});
fs.readFile("./template/main/images/background/background_grass.png", function (err, backGrass) {
    if (err) {
        throw err;
    }
    backgroundGrass = backGrass;
});
fs.readFile("./template/main/images/background/background_flying.png", function (err, backFlying) {
    if (err) {
        throw err;
    }
    backgroundFlying = backFlying;
});
fs.readFile("./template/main/images/background/background_psychic.png", function (err, backPsychic) {
    if (err) {
        throw err;
    }
    backgroundPsychic = backPsychic;
});
fs.readFile("./template/main/images/background/background_bug.png", function (err, backBug) {
    if (err) {
        throw err;
    }
    backgroundBug = backBug;
});
fs.readFile("./template/main/images/background/background_fire.jpg", function (err, backFire) {
    if (err) {
        throw err;
    }
    backgroundFire = backFire;
});
fs.readFile("./template/main/images/background/background_dark.png", function (err, backDark) {
    if (err) {
        throw err;
    }
    backgroundDark = backDark;
});
fs.readFile("./template/main/images/background/background_poison.png", function (err, backPoison) {
    if (err) {
        throw err;
    }
    backgroundPoison = backPoison;
});
fs.readFile("./template/main/images/background/background_ground.jpg", function (err, backGround) {
    if (err) {
        throw err;
    }
    backgroundGround = backGround;
});
fs.readFile("./template/main/images/background/background_fighting.png", function (err, backFighting) {
    if (err) {
        throw err;
    }
    backgroundFighting = backFighting;
});
fs.readFile("./template/main/images/background/background_steel.jpg", function (err, backSteel) {
    if (err) {
        throw err;
    }
    backgroundSteel = backSteel;
});
fs.readFile("./template/main/images/background/background_rock.jpg", function (err, backRock) {
    if (err) {
        throw err;
    }
    backgroundRock = backRock;
});
fs.readFile("./template/main/images/background/background_electric.png", function (err, backElectric) {
    if (err) {
        throw err;
    }
    backgroundElectric = backElectric;
});
fs.readFile("./template/main/images/background/background_dragon.jpg", function (err, backDragon) {
    if (err) {
        throw err;
    }
    backgroundDragon = backDragon;
});
fs.readFile("./template/main/images/background/background_fairy.png", function (err, backFairy) {
    if (err) {
        throw err;
    }
    backgroundFairy = backFairy;
});
fs.readFile("./template/main/images/background/background_ghost.png", function (err, backGhost) {
    if (err) {
        throw err;
    }
    backgroundGhost = backGhost;
});
fs.readFile("./template/main/images/background/background_ice.png", function (err, backIce) {
    if (err) {
        throw err;
    }
    backgroundIce = backIce;
});
fs.readFile("./template/main/images/backgrounImage.jpg", function (err, back) {
    if (err) {
        throw err;
    }
    backgrounfImage = back;
});
fs.readFile("./template/main/images/logo_pokemon_daje.png", function (err, daje) {
    if (err) {
        throw err;
    }
    backgroundLogoPokemons = daje;
});
fs.readFile("./template/main/images/rightpokdexcurtain.png", function (err, right) {
    if (err) {
        throw err;
    }
    rightPokemonCurtain = right;
});
fs.readFile("./template/main/images/leftpokdexcurtain.png", function (err, left) {
    if (err) {
        throw err;
    }
    leftPokemonCurtain = left;
});
fs.readFile("./template/main/images/angle-right-solid.svg", function (err, arrow) {
    if (err) {
        throw err;
    }
    rightArrow = arrow;
});
fs.readFile("./template/main/images/angle-left-solid.svg", function (err, arrow) {
    if (err) {
        throw err;
    }
    leftArrow = arrow;
});
fs.readFile("./template/main/style.css", function (err, css) {
    if (err) {
        throw err;
    }
    styleCss = css;
});

fs.readFile("./template/main/reset.css", function (err, css) {
    if (err) {
        throw err;
    }
    resetCss = css;
});
fs.readFile("./index.html", function (err, html) {
    if (err) {
        throw err;
    }
    indexHTML = html;
});
fs.readFile("./template/main/pokemonscarillion.html", function (err, html) {
    if (err) {
        throw err;
    }
    carillionHTML = html;
});
fs.readFile("./routes.js", function (err, jsData) {
    if (err) {
        throw err;
    }
    routesScript = jsData;
});
fs.readFile("./template/main/fetch.js", function (err, jsData) {
    if (err) {
        throw err;
    }
    fetchScript = jsData;
});
fs.readFile("./template/main/script.js", function (err, jsData) {
    if (err) {
        throw err;
    }
    jsScript = jsData;
});
let server = http.createServer(function (req, res) {
    let path = url.parse(req.url).pathname;
    differntResponse(path,res)
})
function differntResponse(path,res){
    switch (path) {
        case "/type_unknown":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeUnknown);
            res.end();
            break;
        case "/type_water":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeWater);
            res.end();
            break;
        case "/type_normal":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeNormal);
            res.end();
            break;
        case "/type_grass":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeGrass);
            res.end();
            break;
        case "/type_flying":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeFlying);
            res.end();
            break;
        case "/type_psychic":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typePsychic);
            res.end();
            break;
        case "/type_Bug":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeBug);
            res.end();
            break;
        case "/type_fire":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeFire);
            res.end();
            break;
        case "/type_dark":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeDark);
            res.end();
            break;
        case "/type_poison":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typePoison);
            res.end();
            break;
        case "/type_ground":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeGround);
            res.end();
            break;
        case "/type_fighting":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeFighting);
            res.end();
            break;
        case "/type_steel":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeSteel);
            res.end();
            break;
        case "/type_rock":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeRock);
            res.end();
            break;
        case "/type_electric":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeElectric);
            res.end();
            break;
        case "/type_dragon":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeDragon);
            res.end();
            break;
        case "/type_fairy":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeFairy);
            res.end();
            break;
        case "/type_ghost":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeGhost);
            res.end();
            break;
        case "/type_ice":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(typeIce);
            res.end();
            break;
        case "/background_unknown":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundUnknown);
            res.end();
            break;
        case "/background_water":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundWater);
            res.end();
            break;
        case "/background_normal":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundNormal);
            res.end();
            break;
        case "/background_grass":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundGrass);
            res.end();
            break;
        case "/background_flying":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundFlying);
            res.end();
            break;
        case "/background_psychic":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundPsychic);
            res.end();
            break;
        case "/background_bug":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundBug);
            res.end();
            break;
        case "/background_fire":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundFire);
            res.end();
            break;
        case "/background_dark":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundDark);
            res.end();
            break;
        case "/background_poison":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundPoison);
            res.end();
            break;
        case "/background_ground":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundGround);
            res.end();
            break;
        case "/background_fighting":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundFighting);
            res.end();
            break;
        case "/background_steel":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundSteel);
            res.end();
            break;
        case "/background_rock":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundRock);
            res.end();
            break;
        case "/background_electric":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundElectric);
            res.end();
            break;
        case "/background_dragon":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgroundDragon);
            res.end();
            break;
        case "/background_fairy":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundFairy);
            res.end();
            break;
        case "/background_ghost":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundGhost);
            res.end();
            break;
        case "/background_ice":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundIce);
            res.end();
            break;
        case "/backgroundimage.jpg":
            res.writeHead(200, {'Content-Type': 'image/jpg'})
            res.write(backgrounfImage);
            res.end();
            break;
        case "/logo_pokemon_daje.png":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(backgroundLogoPokemons);
            res.end();
            break;
        case "/leftpokdexcurtain.png":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(leftPokemonCurtain);
            res.end();
            break;
        case "/rightpokdexcurtain.png":
            res.writeHead(200, {'Content-Type': 'image/png'})
            res.write(rightPokemonCurtain);
            res.end();
            break;
        case "/leftarrow.svg":
            res.writeHead(200, {'Content-Type': 'image/svg+xml'})
            res.write(leftArrow);
            res.end();
            break;
        case "/rightarrow.svg":
            res.writeHead(200, {'Content-Type': 'image/svg+xml'})
            res.write(rightArrow);
            res.end();
            break;
        case "/style.css":
            res.writeHead(200, {'Content-Type': 'text/css'})
            res.write(styleCss);
            res.end();
            break;
        case "/reset.css":
            res.writeHead(200, {'Content-Type': 'text/css'})
            res.write(resetCss);
            res.end();
            break;
        case "/script.js":
            res.writeHead(200, {'Content-Type': 'text/javascript'})
            res.write(jsScript);
            res.end();
            break;
        case "/fetch.js":
            res.writeHead(200, {'Content-Type': 'text/javascript'})
            res.write(fetchScript);
            res.end();
            break;
        case "/routes.js":
            res.writeHead(200, {'Content-Type': 'text/javascript'})
            res.write(routesScript);
            res.end();
            break;
        case "/template/main/pokemonscarillion.html":
            res.writeHead(200, {'Content-Type': 'text/html'})
            res.write(carillionHTML);
            res.end();
            break;
        default:
            res.writeHead(200, {'Content-Type': 'text/html'})
            res.write(indexHTML);
            res.end();
            break;
    }
}
server.listen(1337, '127.0.0.1');
console.log('Server running at http://127.0.0.1:1337/');