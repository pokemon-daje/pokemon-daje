let http = require('http');
let fs = require('fs');
let url = require('url');
let properties = "";

const buf = fs.readFileSync("./property-front-end.json");
buf.toString('utf8');
properties = JSON.parse(buf)

let documentsInfoData ={
    "/background_ice":{
        "path":"./template/main/images/background/background_ice.png",
        "format":"image/png"
    },
    "/background_ghost":{
        "path":"./template/main/images/background/background_ghost.png",
        "format":"image/png"
    },
    "/background_fairy":{
        "path":"./template/main/images/background/background_fairy.png",
        "format":"image/png"
    },
    "/background_dragon":{
    "path":"./template/main/images/background/background_dragon.jpg",
        "format":"image/jpg"
    },
    "/background_electric":{
        "path":"./template/main/images/background/background_electric.png",
        "format":"image/png"
    },
    "/background_rock":{
        "path":"./template/main/images/background/background_rock.jpg",
        "format":"image/jpg"
    },
    "/background_steel":{
        "path":"./template/main/images/background/background_steel.jpg",
        "format":"image/jpg"
    },
    "/background_fighting":{
        "path":"./template/main/images/background/background_fighting.png",
        "format":"image/png"
    },
    "/background_ground":{
        "path":"./template/main/images/background/background_ground.jpg",
        "format":"image/jpg"
    },
    "/background_poison":{
        "path":"./template/main/images/background/background_poison.png",
        "format":"image/png"
    },
    "/background_dark":{
        "path":"./template/main/images/background/background_dark.png",
        "format":"image/png"
    },
    "/background_fire":{
        "path":"./template/main/images/background/background_fire.jpg",
        "format":"image/jpg"
    },
    "/background_bug":{
        "path":"./template/main/images/background/background_bug.png",
        "format":"image/png"
    },
    "/background_psychic":{
        "path":"./template/main/images/background/background_psychic.png",
        "format":"image/png"
    },
    "/background_flying":{
        "path":"./template/main/images/background/background_flying.png",
        "format":"image/png"
    },
    "/background_grass":{
        "path":"./template/main/images/background/background_grass.png",
        "format":"image/png"
    },
    "/background_normal":{
        "path":"./template/main/images/background/background_normal.jpg",
        "format":"image/jpg"
    },
    "/background_water":{
        "path":"./template/main/images/background/background_water.jpg",
        "format":"image/jpg"
    },
    "/background_unknown":{
        "path":"./template/main/images/type/type_unknown.png",
        "format":"image/png"
    },
    "/type_ice":{
        "path":"./template/main/images/type/type_ice.png",
        "format":"image/png"
    },
    "/type_ghost":{
        "path":"./template/main/images/type/type_ghost.png",
        "format":"image/png"
    },
    "/type_fairy":{
        "path":"./template/main/images/type/type_fairy.png",
        "format":"image/png"
    },
    "/type_dragon":{
        "path":"./template/main/images/type/type_dragon.png",
        "format":"image/png"
    },
    "/type_electric":{
        "path":"./template/main/images/type/type_electric.png",
        "format":"image/png"
    },
    "/type_rock":{
        "path":"./template/main/images/type/type_rock.png",
        "format":"image/png"
    },
    "/type_steel":{
        "path":"./template/main/images/type/type_steel.png",
        "format":"image/png"
    },
    "/type_fighting":{
        "path":"./template/main/images/type/type_fighting.png",
        "format":"image/png"
    },
    "/type_ground":{
        "path":"./template/main/images/type/type_ground.png",
        "format":"image/png"
    },
    "/type_poison":{
        "path":"./template/main/images/type/type_poison.png",
        "format":"image/png"
    },
    "/type_dark":{
        "path":"./template/main/images/type/type_dark.png",
        "format":"image/png"
    },
    "/type_fire":{
        "path":"./template/main/images/type/type_fire.png",
        "format":"image/png"
    },
    "/type_bug":{
        "path":"./template/main/images/type/type_bug.png",
        "format":"image/png"
    },
    "/type_psychic":{
        "path":"./template/main/images/type/type_psychic.png",
        "format":"image/png"
    },
    "/type_flying":{
        "path":"./template/main/images/type/type_flying.png",
        "format":"image/png"
    },
    "/type_grass":{
        "path":"./template/main/images/type/type_flying.png",
        "format":"image/png"
    },
    "/background_image":{
        "path":"./template/main/images/background_image.jpg",
        "format":"image/jpg"
    },
    "/logo_pokemon_daje":{
        "path":"./template/main/images/logo_pokemon_daje.png",
        "format":"image/png"
    },
    "/left_pokedex_curtain":{
        "path":"./template/main/images/left_pokedex_curtain.png",
        "format":"image/png"
    },
    "/right_pokedex_curtain":{
        "path":"./template/main/images/right_pokedex_curtain.png",
        "format":"image/png"
    },
    "/left_arrow":{
        "path":"./template/main/images/angle-left-solid.svg",
        "format":"image/svg+xml"
    },
    "/right_arrow":{
        "path":"./template/main/images/angle-right-solid.svg",
        "format":"image/svg+xml"
    },
    "/style":{
        "path":"./template/main/style.css",
        "format":"text/css"
    },
    "/reset":{
        "path":"./template/main/reset.css",
        "format":"text/css"
    },
    "/":{
        "path":"./index.html",
        "format":"text/html"
    },
    "/pokemons_carillion":{
        "path":"./template/main/pokemons_carillion.html",
        "format":"text/html"
    },
    "/routes":{
        "path":"./routes.js",
        "format":"text/javascript"
    },
    "/fetch":{
        "path":"./template/main/fetch.js",
        "format":"text/javascript"
    },
    "/script":{
        "path":"./template/main/script.js",
        "format":"text/javascript"
    },
    "/property-front-end":{
        "path":"./property-front-end.json",
        "format":"application/json"
    }

}
let dataStorage = new Map()
function readFileFromPath(urlPath,path){
    fs.readFile(path, function (err, data) {
        if (err) {
        }else{
            dataStorage.set(urlPath,data)
        }
    });
}
for([urlPathKey,snglDocument] of Object.entries(documentsInfoData)){
    readFileFromPath(urlPathKey,snglDocument["path"])
}
let server = http.createServer(function (req, res) {
    let urlPath = url.parse(req.url).pathname;
    differentResponseData(urlPath,res)
})
function differentResponseData(path,res){
    let data = dataStorage.get(path);
    if(path !== "/favicon.ico"){
        if(data === null || data === undefined){
            data = dataStorage.get("/")
            path = "/"
        }
        res.writeHead(200, {'Content-Type': documentsInfoData[path]["format"]})
        res.write(data);
        res.end();
    }
}
console.log("server is listening on",`http://localhost:${properties.port}`)
server.listen(properties.port);