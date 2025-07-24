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

import org.springframework.http.ResponseEntity;
import com.webinar.copilot.copilot_demo.exception.PokemonApiException;

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
     * Fetches information about a Pokémon from the given URL and returns it as a ResponseEntity.
     *
     * @param url the URL of the Pokémon API endpoint to fetch data from
     * @return a ResponseEntity containing the Pokémon information if the request is successful,
     *         or a ResponseEntity with a 404 status if the Pokémon is not found
     * @throws PokemonApiException if an error occurs while fetching data from the Pokémon API
     */
    public ResponseEntity<Pokemon> getPokemonInfo(String url) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return ResponseEntity.notFound().build();
            }

            JsonNode pokemonInfoNode = objectMapper.readTree(response.body());
            String name = pokemonInfoNode.path("name").asText();
            String height = pokemonInfoNode.path("height").asText();
            String weight = pokemonInfoNode.path("weight").asText();
            String baseExperience = pokemonInfoNode.path("base_experience").asText();

            Pokemon pokemon = new Pokemon(name, height, weight, baseExperience);
            return ResponseEntity.ok(pokemon);
        } catch (IOException | InterruptedException e) {
            throw new PokemonApiException("An error occurred while fetching data from Pokemon API", e);
        }
    }
}
