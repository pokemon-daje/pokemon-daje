document.addEventListener('DOMContentLoaded', () => {
   /*document.querySelector('form').addEventListener('submit', (event) => {
       event.preventDefault();

       const text = encodeURIComponent(document.getElementById('text').value);*/

       fetch("http://localhost:8080/api/pokemons")
           .then((res) => {
               if(res.ok) {
                   return res.json()
               }
           }).then((pokemons) => {
               console.log(pokemons)
               let ul = '';
               for (let pokemon_spacies of pokemons) {
                   ul += `
                   <li class="card">
                       <div class="img"><img src="${pokemon_spacies.sprite_url}" draggable="false"></div>
                       <h2>${pokemon_spacies.name}</h2>
                       <span>Web Developer</span> 
                   </li>  
                   `
               }
               document.querySelector('ul').innerHTML = ul;
           })
   })


