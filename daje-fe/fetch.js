document.addEventListener('DOMContentLoaded', () => {
   /*document.querySelector('form').addEventListener('submit', (event) => {
       event.preventDefault();

       const text = encodeURIComponent(document.getElementById('text').value);*/

       fetch("http://localhost:8080/api/pokemons", { mode: 'no-cors' })
           .then((res) => {
            console.log(res);
               if(res.ok) {
                   return res.json()
               }
           }).then((response) => {
                const { results, resultCount } = response; //destructuring
                console.log(resultCount)
               let ul = '';
               for (let pokemon_spacies of results) {
                   ul += `
                   <li class="card">
                       <div class="img"><img src="${pokemon_spacies.sprite_url}" draggable="false"></div>
                       <h2>${pokemon_spacies.name}</h2>
                       <span>Web Developer</span> 

                       <button type="button" class="btn" id="btn_modal" data-bs-toggle="modal" data-bs-target="#exampleModal">
                        me voi vedè bene?
                       </button>

                       <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                        <div class="modal-dialog">
                        <div class="modal-content">
                           <div class="modal-header">
                              <h1 class="modal-title fs-5" id="exampleModalLabel">Modal title</h1>
                              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                           </div>
                           <div class="modal-body">
                              ...
                           </div>
                           <div class="modal-footer">
                              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                              <button type="button" class="btn btn-primary">Save changes</button>
                           </div>
                        </div>
                        </div>
                     </div>
                   </li>  
                   `
               }
               document.querySelector('ul').innerHTML = ul;
           })
   })


