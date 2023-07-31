package com.pokemon.daje.model.business_data;

import java.util.Arrays;

public enum TypesEnum {
    ICE("ice", 1),
    GHOST("ghost", 2),
    FAIRY("fairy", 3),
    DRAGON("dragon", 4),
    ELECTRIC("electric", 5),
    ROCK("rock", 6),
    STEEL("steel", 7),
    FIGHTING("fighting", 8),
    GROUND("ground", 9),
    POISON("poison", 10),
    DARK("dark", 11),
    FIRE("fire", 12),
    BUG("bug", 13),
    PSYCHIC("psychic", 14),
    FLYING("flying", 15),
    GRASS("grass", 16),
    NORMAL("normal", 17),
    WATER("water", 18),
    UNKNOWN("unknown", 19);

    private final String name;

    private final int id;

    TypesEnum(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public static TypesEnum fromString(String name) {
        return Arrays.stream(TypesEnum.values())
            .filter(type -> type.getName().equals(name.toLowerCase())).findFirst()
            .orElse(UNKNOWN);
    }
}
