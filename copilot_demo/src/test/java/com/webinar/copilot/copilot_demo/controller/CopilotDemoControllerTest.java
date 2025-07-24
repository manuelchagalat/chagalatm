package com.webinar.copilot.copilot_demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.webinar.copilot.copilot_demo.exception.PokemonApiException;
import com.webinar.copilot.copilot_demo.model.Pokemon;
import com.webinar.copilot.copilot_demo.model.PokemonReduce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CopilotDemoControllerTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CopilotDemoController copilotDemoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWaterTypePokemon_Success() throws Exception {
        // Mock API response
        String mockApiResponse = """
            {
                "pokemon": [
                    {
                        "pokemon": {
                            "name": "squirtle",
                            "url": "https://pokeapi.co/api/v2/pokemon/7/"
                        }
                    },
                    {
                        "pokemon": {
                            "name": "wartortle",
                            "url": "https://pokeapi.co/api/v2/pokemon/8/"
                        }
                    }
                ]
            }
        """;

        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockApiResponse);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockResponse);

        // Mock ObjectMapper behavior
        JsonNode mockJsonNode = new ObjectMapper().readTree(mockApiResponse);
        when(objectMapper.readTree(mockApiResponse)).thenReturn(mockJsonNode);

        // Call the method
        ResponseEntity<List<PokemonReduce>> response = copilotDemoController.getWaterTypePokemon();

        // Verify the response
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("squirtle", response.getBody().get(0).getName());
        assertEquals("wartortle", response.getBody().get(1).getName());
    }

    @Test
    void testGetPokemonInfo_Success() throws Exception {
        // Mock API response
        String mockApiResponse = """
            {
                "name": "pikachu",
                "height": "4",
                "weight": "60",
                "base_experience": "112"
            }
        """;

        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockApiResponse);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockResponse);

        // Mock ObjectMapper behavior
        JsonNode mockJsonNode = new ObjectMapper().readTree(mockApiResponse);
        when(objectMapper.readTree(mockApiResponse)).thenReturn(mockJsonNode);

        // Call the method
        ResponseEntity<Pokemon> response = copilotDemoController.getPokemonInfo("https://pokeapi.co/api/v2/pokemon/25/");

        // Verify the response
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("pikachu", response.getBody().getName());
        assertEquals("4", response.getBody().getHeight());
        assertEquals("60", response.getBody().getWeight());
        assertEquals("112", response.getBody().getBaseExperience());
    }

    @Test
    void testGetWaterTypePokemon_ApiError() throws Exception {
        // Mock API response with error status
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockResponse);

        // Verify exception is thrown
        PokemonApiException exception = assertThrows(PokemonApiException.class, () -> copilotDemoController.getWaterTypePokemon());
        assertTrue(exception.getMessage().contains("Failed to fetch data from Pokemon API"));
    }

    @Test
    void testGetWaterTypePokemon_IOException() throws Exception {
        // Mock IOException during API call
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);

        // Verify exception is thrown
        PokemonApiException exception = assertThrows(PokemonApiException.class, () -> copilotDemoController.getWaterTypePokemon());
        assertTrue(exception.getMessage().contains("An error occurred while fetching data from Pokemon API"));
    }

    @Test
    void testGetWaterTypePokemon_InterruptedException() throws Exception {
        // Mock InterruptedException during API call
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(InterruptedException.class);

        // Verify exception is thrown
        PokemonApiException exception = assertThrows(PokemonApiException.class, () -> copilotDemoController.getWaterTypePokemon());
        assertTrue(exception.getMessage().contains("An error occurred while fetching data from Pokemon API"));
    }

    @Test
    void testGetPokemonInfo_NotFound() throws Exception {
        // Mock API response with 404 status
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(404);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockResponse);

        // Call the method
        ResponseEntity<Pokemon> response = copilotDemoController.getPokemonInfo("https://pokeapi.co/api/v2/pokemon/9999/");

        // Verify the response
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testGetPokemonInfo_IOException() throws Exception {
        // Mock IOException during API call
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);

        // Verify exception is thrown
        PokemonApiException exception = assertThrows(PokemonApiException.class, () -> copilotDemoController.getPokemonInfo("https://pokeapi.co/api/v2/pokemon/25/"));
        assertTrue(exception.getMessage().contains("An error occurred while fetching data from Pokemon API"));
    }

    @Test
    void testGetPokemonInfo_InterruptedException() throws Exception {
        // Mock InterruptedException during API call
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(InterruptedException.class);

        // Verify exception is thrown
        PokemonApiException exception = assertThrows(PokemonApiException.class, () -> copilotDemoController.getPokemonInfo("https://pokeapi.co/api/v2/pokemon/25/"));
        assertTrue(exception.getMessage().contains("An error occurred while fetching data from Pokemon API"));
    }
}