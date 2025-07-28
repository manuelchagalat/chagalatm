package com.webinar.copilot.copilot_demo.enums;

/**
 * Enum que define los tipos de Pokémon disponibles en la API
 * con sus respectivos IDs y nombres.
 * 
 * @author David Guerra
 */
public enum PokemonType {
    NORMAL(1, "normal"),
    FIGHTING(2, "fighting"),
    FLYING(3, "flying"), 
    POISON(4, "poison"),
    GROUND(5, "ground"),
    ROCK(6, "rock"),
    BUG(7, "bug"),
    GHOST(8, "ghost"),
    STEEL(9, "steel"),
    FIRE(10, "fire"),
    WATER(11, "water"),
    GRASS(12, "grass"),
    ELECTRIC(13, "electric"),
    PSYCHIC(14, "psychic"),
    ICE(15, "ice"),
    DRAGON(16, "dragon"),
    DARK(17, "dark"),
    FAIRY(18, "fairy");

    private final int id;
    private final String name;

    PokemonType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Construye la URL de la API para obtener Pokémon de este tipo
     * @return URL de la API para el tipo específico
     */
    public String getApiUrl() {
        return "https://pokeapi.co/api/v2/type/" + this.id;
    }

    /**
     * Obtiene un tipo de Pokémon por su ID
     * @param id el ID del tipo
     * @return el tipo de Pokémon correspondiente
     * @throws IllegalArgumentException si no existe un tipo con ese ID
     */
    public static PokemonType getById(int id) {
        for (PokemonType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("No existe un tipo de Pokémon con ID: " + id);
    }

    /**
     * Obtiene un tipo de Pokémon por su nombre
     * @param name el nombre del tipo
     * @return el tipo de Pokémon correspondiente
     * @throws IllegalArgumentException si no existe un tipo con ese nombre
     */
    public static PokemonType getByName(String name) {
        for (PokemonType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No existe un tipo de Pokémon con nombre: " + name);
    }
}
