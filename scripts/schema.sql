create table pokemons
(
    current_health_points int          null,
    id                    int auto_increment primary key,
    max_health_points     int          null,
    pokedex_id            int          null,
    name                  varchar(255) null,
    sprite_url            varchar(255) null,
    trainer_name          varchar(255) null
);

create table type
(
    db_id           int auto_increment primary key,
    pokedex_type_id int          null,
    image_url       varchar(255) null,
    name            varchar(255) null
);

create table move
(
    id              int auto_increment primary key,
    pokedex_move_id int          null,
    power           int          null,
    type_id         int          null,
    name            varchar(255) null,
    foreign key (type_id) references type (db_id)
);

create table pokemons_move_set
(
    move_db_id    int not null,
    pokemon_db_id int not null,
    primary key (move_db_id, pokemon_db_id),
    foreign key (pokemon_db_id) references pokemons (id),
    foreign key (move_db_id) references move (id)
);

