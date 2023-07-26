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