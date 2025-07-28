package com.webinar.copilot.copilot_demo.constants;

/**
 * Constantes para la aplicación de Pokémon
 * 
 * @author David Guerra
 */
public final class PokemonConstants {
    
    private PokemonConstants() {
        // Constructor privado para evitar instanciación
    }
    
    // URLs base de la API
    public static final String POKEMON_API_BASE_URL = "https://pokeapi.co/api/v2";
    public static final String POKEMON_ENDPOINT = "/pokemon/";
    public static final String TYPE_ENDPOINT = "/type/";
    
    // Mensajes de error en español
    public static final String ERROR_FETCHING_POKEMON_DATA = "Error al obtener datos del Pokémon desde la API";
    public static final String ERROR_FETCHING_TYPE_DATA = "Error al obtener datos del tipo de Pokémon desde la API";
    public static final String ERROR_POKEMON_NOT_FOUND = "Pokémon no encontrado";
    public static final String ERROR_INVALID_REQUEST = "Solicitud inválida";
    public static final String ERROR_API_UNAVAILABLE = "La API de Pokémon no está disponible";
    public static final String ERROR_PARSING_RESPONSE = "Error al procesar la respuesta de la API";
    public static final String ERROR_NETWORK_CONNECTION = "Error de conexión de red";
    
    // Códigos de estado HTTP
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    
    // Límites y configuraciones
    public static final int DEFAULT_POKEMON_LIMIT = 10;
    public static final int MAX_POKEMON_BATCH_SIZE = 50;
    public static final int MIN_POKEMON_NAME_LENGTH = 1;
    public static final int MAX_POKEMON_NAME_LENGTH = 20;
    
    /**
     * Construye la URL completa para obtener información de un Pokémon
     * @param nameOrId nombre o ID del Pokémon
     * @return URL completa
     */
    public static String buildPokemonUrl(String nameOrId) {
        return POKEMON_API_BASE_URL + POKEMON_ENDPOINT + nameOrId;
    }
    
    /**
     * Construye la URL completa para obtener Pokémon de un tipo específico
     * @param typeId ID del tipo de Pokémon
     * @return URL completa
     */
    public static String buildTypeUrl(int typeId) {
        return POKEMON_API_BASE_URL + TYPE_ENDPOINT + typeId;
    }
}
