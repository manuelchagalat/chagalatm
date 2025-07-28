package com.webinar.copilot.copilot_demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import com.webinar.copilot.copilot_demo.exception.PokemonApiException;
import com.webinar.copilot.copilot_demo.constants.PokemonConstants;
import com.webinar.copilot.copilot_demo.enums.PokemonType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webinar.copilot.copilot_demo.model.Pokemon;
import com.webinar.copilot.copilot_demo.model.PokemonReduce;

/**
 * This controller provides an endpoint to fetch a list of water-type Pokémon
 * from the Pokémon API.
 * 
 * <p>The endpoint makes an HTTP GET request to the Pokémon API to retrieve
 * data about water-type Pokémon. The response is parsed and transformed into
 * a list of {@link Pokemon} objects, which are then returned as the response.
 * 
 * <p>Endpoint:
 * <ul>
 *   <li><b>GET /water-type-pokemon</b>: Fetches a list of water-type Pokémon.</li>
 * </ul>
 * 
 * @author David Guerra
 * @see <a href="https://pokeapi.co/">Pokémon API</a>
 * @see Pokemon
 * @see PokemonApiException
 */
@RestController
public class CopilotDemoController {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles GET requests to retrieve a list of water-type Pokémon.
     *
     * This method fetches data from the Pokémon API for water-type Pokémon
     * and returns a list of Pokémon objects containing their names and URLs.
     *
     * @return ResponseEntity containing a list of {@link PokemonReduce} objects representing water-type Pokémon.
     * @throws PokemonApiException if there is an error while fetching data from the Pokémon API
     *                             or if the API response status code is not 200.
     */
    @GetMapping("/water-type-pokemon")
    public ResponseEntity<List<PokemonReduce>> getWaterTypePokemon() {
        String pokemonApiUrl = "https://pokeapi.co/api/v2/type/11";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(pokemonApiUrl))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new PokemonApiException("Failed to fetch data from Pokemon API. Status code: " + response.statusCode());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode pokemonNode = rootNode.path("pokemon");

            List<PokemonReduce> pokemonList = new ArrayList<>();
            for (JsonNode node : pokemonNode) {
                JsonNode pokemonInfo = node.path("pokemon");
                String name = pokemonInfo.path("name").asText();
                String url = pokemonInfo.path("url").asText();
                pokemonList.add(new PokemonReduce(name, url));
            }

            return ResponseEntity.ok(pokemonList);
        } catch (IOException | InterruptedException e) {
            throw new PokemonApiException("An error occurred while fetching data from Pokemon API", e);
        }
    }

    /**
     * Obtiene información detallada de un Pokémon desde una URL específica.
     * 
     * <p>Método mejorado que incluye:
     * <ul>
     *   <li>Validación de entrada</li>
     *   <li>Uso de constantes en lugar de números mágicos</li>
     *   <li>Manejo detallado de códigos de estado HTTP</li>
     *   <li>Mensajes de error en español</li>
     *   <li>Validación de campos JSON</li>
     * </ul>
     *
     * @param url la URL del endpoint de la API de Pokémon
     * @return ResponseEntity con la información del Pokémon si la petición es exitosa,
     *         o una respuesta de error apropiada si no se encuentra o hay problemas
     * @throws PokemonApiException si ocurre un error al obtener datos de la API
     */
    public ResponseEntity<Pokemon> getPokemonInfo(String url) {
        // Validación de entrada
        if (url == null || url.trim().isEmpty()) {
            throw new PokemonApiException(PokemonConstants.ERROR_INVALID_REQUEST + ": URL no puede estar vacía");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(java.time.Duration.ofSeconds(10)) // Timeout de 10 segundos
            .header("User-Agent", "Pokemon-Demo-App/1.0")
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Manejo detallado de códigos de estado HTTP
            return switch (response.statusCode()) {
                case PokemonConstants.HTTP_OK -> processPokemonResponse(response.body());
                case PokemonConstants.HTTP_NOT_FOUND -> ResponseEntity.notFound().build();
                case PokemonConstants.HTTP_BAD_REQUEST -> ResponseEntity.badRequest().build();
                case PokemonConstants.HTTP_INTERNAL_SERVER_ERROR -> {
                    throw new PokemonApiException(PokemonConstants.ERROR_API_UNAVAILABLE + 
                        ". Código de estado: " + response.statusCode());
                }
                default -> {
                    throw new PokemonApiException(PokemonConstants.ERROR_FETCHING_POKEMON_DATA + 
                        ". Código de estado inesperado: " + response.statusCode());
                }
            };
            
        } catch (IOException e) {
            throw new PokemonApiException(PokemonConstants.ERROR_NETWORK_CONNECTION + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            throw new PokemonApiException(PokemonConstants.ERROR_FETCHING_POKEMON_DATA + 
                ": Operación interrumpida", e);
        } catch (Exception e) {
            throw new PokemonApiException(PokemonConstants.ERROR_FETCHING_POKEMON_DATA + 
                ": Error inesperado - " + e.getMessage(), e);
        }
    }

    /**
     * Procesa la respuesta JSON de la API y crea un objeto Pokemon.
     * 
     * @param responseBody el cuerpo de la respuesta JSON
     * @return ResponseEntity con el objeto Pokemon
     * @throws PokemonApiException si hay errores al procesar el JSON
     */
    private ResponseEntity<Pokemon> processPokemonResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new PokemonApiException(PokemonConstants.ERROR_PARSING_RESPONSE + 
                    ": Respuesta vacía de la API");
            }

            JsonNode pokemonInfoNode = objectMapper.readTree(responseBody);
            
            // Validar que el JSON contiene los campos requeridos
            if (pokemonInfoNode.isMissingNode()) {
                throw new PokemonApiException(PokemonConstants.ERROR_PARSING_RESPONSE + 
                    ": Estructura JSON inválida");
            }

            // Extraer campos con validación y valores por defecto
            String name = extractJsonField(pokemonInfoNode, "name", "Nombre desconocido");
            String height = extractJsonField(pokemonInfoNode, "height", "0");
            String weight = extractJsonField(pokemonInfoNode, "weight", "0");
            String baseExperience = extractJsonField(pokemonInfoNode, "base_experience", "0");

            // Validar que el nombre no esté vacío (campo crítico)
            if (name.equals("Nombre desconocido") || name.trim().isEmpty()) {
                throw new PokemonApiException(PokemonConstants.ERROR_PARSING_RESPONSE + 
                    ": Nombre del Pokémon no encontrado en la respuesta");
            }

            Pokemon pokemon = new Pokemon(name, height, weight, baseExperience);
            return ResponseEntity.ok(pokemon);
            
        } catch (IOException e) {
            throw new PokemonApiException(PokemonConstants.ERROR_PARSING_RESPONSE + 
                ": Error al procesar JSON - " + e.getMessage(), e);
        }
    }

    /**
     * Extrae un campo del JSON de forma segura con valor por defecto.
     * 
     * @param jsonNode el nodo JSON
     * @param fieldName el nombre del campo
     * @param defaultValue el valor por defecto si el campo no existe
     * @return el valor del campo o el valor por defecto
     */
    private String extractJsonField(JsonNode jsonNode, String fieldName, String defaultValue) {
        JsonNode fieldNode = jsonNode.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        String value = fieldNode.asText();
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    // ========================================
    // 🎯 EJEMPLOS DE FUNCTION LAMBDA
    // ========================================

    /**
     * EJEMPLO 1: Function básica - Transformar nombre a mayúsculas
     * Function<String, String> - Toma String, retorna String
     */
    @GetMapping("/demo/function-uppercase/{pokemonName}")
    public ResponseEntity<String> demoFunctionUppercase(@PathVariable String pokemonName) {
        // Function que convierte a mayúsculas
        Function<String, String> toUpperCase = name -> name.toUpperCase();
        
        String result = toUpperCase.apply(pokemonName);
        return ResponseEntity.ok("Pokémon en mayúsculas: " + result);
    }

    /**
     * EJEMPLO 2: Function para calcular longitud del nombre
     * Function<String, Integer> - Toma String, retorna Integer
     */
    @GetMapping("/demo/function-length/{pokemonName}")
    public ResponseEntity<String> demoFunctionLength(@PathVariable String pokemonName) {
        // Function que calcula la longitud
        Function<String, Integer> getLength = name -> name.length();
        
        Integer length = getLength.apply(pokemonName);
        return ResponseEntity.ok("El nombre '" + pokemonName + "' tiene " + length + " caracteres");
    }

    /**
     * EJEMPLO 3: Function compuesta - Transformar y formatear
     * Combina múltiples transformaciones
     */
    @GetMapping("/demo/function-compose/{pokemonName}")
    public ResponseEntity<String> demoFunctionCompose(@PathVariable String pokemonName) {
        // Functions individuales
        Function<String, String> toUpperCase = name -> name.toUpperCase();
        Function<String, String> addEmoji = name -> "🔥 " + name + " ⚡";
        Function<String, String> addPrefix = name -> "POKÉMON: " + name;
        
        // Componer functions usando andThen()
        Function<String, String> composedFunction = toUpperCase
            .andThen(addEmoji)
            .andThen(addPrefix);
        
        String result = composedFunction.apply(pokemonName);
        return ResponseEntity.ok(result);
    }

    /**
     * EJEMPLO 4: Function con lógica compleja - Clasificar Pokémon por longitud de nombre
     * Function<String, String> - Clasifica el Pokémon
     */
    @GetMapping("/demo/function-classify/{pokemonName}")
    public ResponseEntity<String> demoFunctionClassify(@PathVariable String pokemonName) {
        // Function que clasifica por longitud del nombre
        Function<String, String> classifyPokemon = name -> {
            int length = name.length();
            if (length <= 4) return "Nombre Corto 📏";
            else if (length <= 8) return "Nombre Medio 📐";
            else return "Nombre Largo 📏📏";
        };
        
        String classification = classifyPokemon.apply(pokemonName);
        return ResponseEntity.ok("Clasificación: " + classification);
    }

    /**
     * EJEMPLO 5: Function con Map - Transformar lista de nombres
     * Usa Function dentro de un stream para transformar colecciones
     */
    @GetMapping("/demo/function-transform-list")
    public ResponseEntity<List<String>> demoFunctionTransformList() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "blastoise", "venusaur");
        
        // Function para formatear nombres
        Function<String, String> formatName = name -> 
            "🎮 " + name.substring(0, 1).toUpperCase() + name.substring(1) + " ⭐";
        
        // Aplicar Function a todos los elementos usando map()
        List<String> formattedNames = pokemonNames.stream()
            .map(formatName)  // Aquí usamos la Function
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(formattedNames);
    }

    /**
     * EJEMPLO 6: Function para convertir PokemonReduce a formato personalizado
     * Function<PokemonReduce, String> - Convierte objeto a String
     */
    @GetMapping("/demo/function-pokemon-format")
    public ResponseEntity<List<String>> demoFunctionPokemonFormat() {
        // Simular algunos datos (en un caso real vendría de la API)
        List<PokemonReduce> pokemonList = List.of(
            new PokemonReduce("pikachu", "https://pokeapi.co/api/v2/pokemon/25/"),
            new PokemonReduce("charizard", "https://pokeapi.co/api/v2/pokemon/6/"),
            new PokemonReduce("blastoise", "https://pokeapi.co/api/v2/pokemon/9/")
        );
        
        // Function que convierte PokemonReduce a formato personalizado
        Function<PokemonReduce, String> formatPokemon = pokemon -> {
            String id = pokemon.getUrl().replaceAll(".*/(\\d+)/$", "$1");
            return String.format("ID: %s | Nombre: %s | URL: %s", 
                id, pokemon.getName().toUpperCase(), pokemon.getUrl());
        };
        
        // Aplicar la transformación
        List<String> formattedPokemon = pokemonList.stream()
            .map(formatPokemon)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(formattedPokemon);
    }

    /**
     * EJEMPLO 7: Function anidadas - Múltiples transformaciones
     * Demuestra cómo usar Functions dentro de otras Functions
     */
    @GetMapping("/demo/function-nested/{input}")
    public ResponseEntity<String> demoFunctionNested(@PathVariable String input) {
        // Function que procesa texto
        Function<String, String> processText = text -> text.toLowerCase().trim();
        
        // Function que valida y formatea
        Function<String, String> validateAndFormat = text -> {
            String processed = processText.apply(text);  // Usar otra Function aquí
            if (processed.length() < 3) {
                return "⚠️ Nombre muy corto: " + processed;
            }
            return "✅ Nombre válido: " + processed.toUpperCase();
        };
        
        String result = validateAndFormat.apply(input);
        return ResponseEntity.ok(result);
    }

    /**
     * EJEMPLO 8: Function con Predicate - Combinar interfaces funcionales
     * Muestra cómo usar Function junto con Predicate
     */
    @GetMapping("/demo/function-with-predicate")
    public ResponseEntity<List<String>> demoFunctionWithPredicate() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff");
        
        // Predicate para filtrar nombres largos
        Predicate<String> isLongName = name -> name.length() > 7;
        
        // Function para transformar nombres
        Function<String, String> makeSpecial = name -> "⭐ " + name.toUpperCase() + " ⭐";
        
        // Combinar Predicate y Function en un stream
        List<String> result = pokemonNames.stream()
            .filter(isLongName)           // Primero filtramos
            .map(makeSpecial)            // Luego transformamos con Function
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    // ========================================
    // 🌊 EJEMPLOS DE STREAMS CON LAMBDAS
    // ========================================

    /**
     * EJEMPLO 1: Filter - Filtrar elementos que cumplen una condición
     * Stream.filter(Predicate<T> predicate)
     */
    @GetMapping("/demo/stream-filter/{minLength}")
    public ResponseEntity<List<String>> demoStreamFilter(@PathVariable int minLength) {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff", "mew");
        
        // Filtrar Pokémon con nombres de longitud mínima
        List<String> filteredPokemon = pokemonNames.stream()
            .filter(name -> name.length() >= minLength)  // Lambda como Predicate
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(filteredPokemon);
    }

    /**
     * EJEMPLO 2: Map - Transformar cada elemento
     * Stream.map(Function<T, R> mapper)
     */
    @GetMapping("/demo/stream-map")
    public ResponseEntity<List<String>> demoStreamMap() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur");
        
        // Transformar nombres a formato especial
        List<String> transformedNames = pokemonNames.stream()
            .map(name -> "🎮 " + name.toUpperCase() + " ⚡")  // Lambda como Function
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transformedNames);
    }

    /**
     * EJEMPLO 3: Sorted - Ordenar elementos
     * Stream.sorted() y Stream.sorted(Comparator<T> comparator)
     */
    @GetMapping("/demo/stream-sorted")
    public ResponseEntity<List<String>> demoStreamSorted() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff");
        
        // Ordenar por longitud del nombre (descendente)
        List<String> sortedByLength = pokemonNames.stream()
            .sorted((name1, name2) -> Integer.compare(name2.length(), name1.length()))  // Lambda como Comparator
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(sortedByLength);
    }

    /**
     * EJEMPLO 4: Distinct - Eliminar duplicados
     * Stream.distinct()
     */
    @GetMapping("/demo/stream-distinct")
    public ResponseEntity<List<String>> demoStreamDistinct() {
        List<String> pokemonTypes = List.of("fire", "water", "grass", "fire", "electric", "water", "psychic");
        
        // Eliminar tipos duplicados y ordenar
        List<String> uniqueTypes = pokemonTypes.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(uniqueTypes);
    }

    /**
     * EJEMPLO 5: Limit y Skip - Paginación
     * Stream.limit(long maxSize) y Stream.skip(long n)
     */
    @GetMapping("/demo/stream-pagination/{page}/{size}")
    public ResponseEntity<List<String>> demoStreamPagination(@PathVariable int page, @PathVariable int size) {
        List<String> allPokemon = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff", 
                                         "geodude", "machop", "alakazam", "gengar", "onix");
        
        // Implementar paginación simple
        List<String> paginatedPokemon = allPokemon.stream()
            .skip((long) page * size)  // Saltar elementos de páginas anteriores
            .limit(size)               // Limitar al tamaño de página
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(paginatedPokemon);
    }

    /**
     * EJEMPLO 6: Reduce - Combinar elementos en un solo resultado
     * Stream.reduce(BinaryOperator<T> accumulator)
     */
    @GetMapping("/demo/stream-reduce")
    public ResponseEntity<String> demoStreamReduce() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur");
        
        // Concatenar todos los nombres con separador
        String concatenatedNames = pokemonNames.stream()
            .reduce("", (acc, name) -> acc.isEmpty() ? name : acc + " | " + name);  // Lambda como BinaryOperator
        
        return ResponseEntity.ok("Pokémon concatenados: " + concatenatedNames);
    }

    /**
     * EJEMPLO 7: Count - Contar elementos
     * Stream.count()
     */
    @GetMapping("/demo/stream-count/{minLength}")
    public ResponseEntity<String> demoStreamCount(@PathVariable int minLength) {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff", "mew");
        
        // Contar Pokémon con nombres largos
        long count = pokemonNames.stream()
            .filter(name -> name.length() >= minLength)
            .count();
        
        return ResponseEntity.ok("Pokémon con nombres de " + minLength + "+ caracteres: " + count);
    }

    /**
     * EJEMPLO 8: FindFirst y FindAny - Buscar elementos
     * Stream.findFirst() y Stream.findAny()
     */
    @GetMapping("/demo/stream-find/{startsWith}")
    public ResponseEntity<String> demoStreamFind(@PathVariable String startsWith) {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff");
        
        // Buscar el primer Pokémon que empiece con las letras especificadas
        String result = pokemonNames.stream()
            .filter(name -> name.startsWith(startsWith.toLowerCase()))
            .findFirst()
            .orElse("No encontrado");
        
        return ResponseEntity.ok("Primer Pokémon encontrado: " + result);
    }

    /**
     * EJEMPLO 9: AnyMatch, AllMatch, NoneMatch - Verificaciones booleanas
     * Stream.anyMatch(), Stream.allMatch(), Stream.noneMatch()
     */
    @GetMapping("/demo/stream-match/{length}")
    public ResponseEntity<String> demoStreamMatch(@PathVariable int length) {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur");
        
        boolean anyMatch = pokemonNames.stream().anyMatch(name -> name.length() > length);
        boolean allMatch = pokemonNames.stream().allMatch(name -> name.length() > 3);
        boolean noneMatch = pokemonNames.stream().noneMatch(name -> name.length() > 15);
        
        String result = String.format(
            "¿Alguno > %d chars? %s | ¿Todos > 3 chars? %s | ¿Ninguno > 15 chars? %s",
            length, anyMatch, allMatch, noneMatch
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * EJEMPLO 10: Parallel Streams - Procesamiento en paralelo
     * collection.parallelStream()
     */
    @GetMapping("/demo/stream-parallel")
    public ResponseEntity<List<String>> demoStreamParallel() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", 
                                           "jigglypuff", "geodude", "machop", "alakazam");
        
        // Procesar en paralelo (útil para operaciones costosas)
        List<String> processedNames = pokemonNames.parallelStream()
            .map(name -> {
                // Simular operación costosa
                try { Thread.sleep(10); } catch (InterruptedException e) { }
                return "🚀 " + name.toUpperCase() + " (processed)";
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(processedNames);
    }

    /**
     * EJEMPLO 11: Grouping - Agrupar elementos por criterio
     * Collectors.groupingBy()
     */
    @GetMapping("/demo/stream-grouping")
    public ResponseEntity<java.util.Map<Integer, List<String>>> demoStreamGrouping() {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", 
                                           "jigglypuff", "mew", "alakazam");
        
        // Agrupar por longitud del nombre
        java.util.Map<Integer, List<String>> groupedByLength = pokemonNames.stream()
            .collect(Collectors.groupingBy(String::length));  // Method reference
        
        return ResponseEntity.ok(groupedByLength);
    }

    /**
     * EJEMPLO 12: Partitioning - Dividir en dos grupos
     * Collectors.partitioningBy()
     */
    @GetMapping("/demo/stream-partitioning/{length}")
    public ResponseEntity<java.util.Map<Boolean, List<String>>> demoStreamPartitioning(@PathVariable int length) {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", "jigglypuff", "mew");
        
        // Dividir en nombres cortos y largos
        java.util.Map<Boolean, List<String>> partitioned = pokemonNames.stream()
            .collect(Collectors.partitioningBy(name -> name.length() > length));
        
        return ResponseEntity.ok(partitioned);
    }

    /**
     * EJEMPLO 13: Operaciones complejas encadenadas
     * Combinando múltiples operaciones intermediate y terminales
     */
    @GetMapping("/demo/stream-complex/{minLength}/{maxResults}")
    public ResponseEntity<List<String>> demoStreamComplex(@PathVariable int minLength, @PathVariable int maxResults) {
        List<String> pokemonNames = List.of("pikachu", "charizard", "squirtle", "bulbasaur", 
                                           "jigglypuff", "geodude", "machop", "alakazam", "gengar", "onix");
        
        // Operación compleja: filtrar, transformar, ordenar, limitar
        List<String> result = pokemonNames.stream()
            .filter(name -> name.length() >= minLength)              // Filtrar por longitud
            .map(name -> name.toUpperCase())                         // Convertir a mayúsculas
            .map(name -> "⭐ " + name + " ⭐")                       // Agregar decoración
            .sorted()                                                // Ordenar alfabéticamente
            .limit(maxResults)                                       // Limitar resultados
            .collect(Collectors.toList());                          // Recolectar a lista
        
        return ResponseEntity.ok(result);
    }

    /**
     * EJEMPLO 14: Trabajando con objetos complejos (PokemonReduce)
     * Streams con objetos personalizados
     */
    @GetMapping("/demo/stream-objects")
    public ResponseEntity<List<String>> demoStreamObjects() {
        List<PokemonReduce> pokemonList = List.of(
            new PokemonReduce("pikachu", "https://pokeapi.co/api/v2/pokemon/25/"),
            new PokemonReduce("charizard", "https://pokeapi.co/api/v2/pokemon/6/"),
            new PokemonReduce("blastoise", "https://pokeapi.co/api/v2/pokemon/9/"),
            new PokemonReduce("venusaur", "https://pokeapi.co/api/v2/pokemon/3/")
        );
        
        // Extraer IDs, filtrar y formatear
        List<String> processedPokemon = pokemonList.stream()
            .filter(pokemon -> pokemon.getName().length() > 6)      // Filtrar por nombre largo
            .map(pokemon -> {                                       // Extraer ID de URL
                String id = pokemon.getUrl().replaceAll(".*/(\\d+)/$", "$1");
                return String.format("ID: %s - %s", id, pokemon.getName().toUpperCase());
            })
            .sorted()                                               // Ordenar
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(processedPokemon);
    }

    /**
     * EJEMPLO 15: FlatMap - Aplanar streams anidados
     * Stream.flatMap()
     */
    @GetMapping("/demo/stream-flatmap")
    public ResponseEntity<List<String>> demoStreamFlatMap() {
        List<List<String>> pokemonByGeneration = List.of(
            List.of("pikachu", "charizard", "squirtle"),     // Gen 1
            List.of("chikorita", "cyndaquil", "totodile"),   // Gen 2
            List.of("treecko", "torchic", "mudkip")          // Gen 3
        );
        
        // Aplanar todas las listas en una sola
        List<String> allPokemon = pokemonByGeneration.stream()
            .flatMap(List::stream)                           // Aplanar listas
            .map(name -> "Gen Mix: " + name.toUpperCase())   // Transformar
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(allPokemon);
    }
}
