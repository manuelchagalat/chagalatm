import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

package com.mcht.copilot;



class LambdaExamplesTest {

    @Test
    void testOperacionSuma() {
        LambdaExamples.Operacion suma = (a, b) -> a + b;
        assertEquals(8, suma.aplicar(5, 3));
        assertEquals(0, suma.aplicar(-2, 2));
    }

    @Test
    void testPredicateEsVacio() {
        assertTrue(((java.util.function.Predicate<String>) String::isEmpty).test(""));
        assertFalse(((java.util.function.Predicate<String>) String::isEmpty).test("abc"));
    }

    @Test
    void testFunctionLongitud() {
        java.util.function.Function<String, Integer> longitud = String::length;
        assertEquals(7, longitud.apply("Copilot"));
        assertEquals(0, longitud.apply(""));
    }

    @Test
    void testPruebaStremConversion() {
        List<Integer> numeros = Arrays.asList(1, 2, 3, 4, 5);
        List<String> resultado = LambdaExamples.pruebaStrem(numeros);
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), resultado);
    }
}