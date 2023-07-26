document.addEventListener("DOMContentLoaded", () => {
    const urlRoutes = {
        "/": {
            template: "../template/main/pokemonscarillion.html"
        }
    }
    const urlRoute = (event) => {
        if (window !== undefined) {
            event = event || window.event;
            event.preventDefault();
            window.history.pushState({}, "", "/");
            urlLocationHandler();
        }
    }
    const urlLocationHandler = async () => {
        if (window !== undefined) {
            let location = window.location.pathname;
            if (location.length === 0) {
                location = "/"
            }

            const route = urlRoutes[location];
            let elementTemplate = document.querySelector(".content-to-change");
            let fakeHtml = document.createElement("html")
            fakeHtml.innerHTML = await fetch(route.template)
                .then((response) => response.text());
            elementTemplate.innerHTML = fakeHtml.querySelector(".container-wrapper").innerHTML;
        }
    }
    window.onpopstate = urlLocationHandler();
    window.route = urlRoute;
    urlLocationHandler();
})