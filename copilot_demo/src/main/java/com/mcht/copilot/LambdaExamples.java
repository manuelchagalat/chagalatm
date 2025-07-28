package com.mcht.copilot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.expression.spel.ast.Operator;

public class LambdaExamples {
    /**
     * Interfaz funcional personalizada para operaciones matemáticas.
     */
    @FunctionalInterface
    public interface Operacion {
        // Método abstracto para realizar una operación entre dos números
        int aplicar(int a, int b);
    }

	public static void main(String[] args) {
		//Crear una lista de numeros enteros
		List<Integer> numeros = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		pruebaStrem(numeros);
		
        // Ejemplo de uso de la interfaz funcional personalizada
        Operacion suma = (a, b) -> a + b; // Suma dos números
        int resultado = suma.aplicar(5, 3); // resultado = 8
        System.out.println("Suma: " + resultado);

        // Ejemplo de Predicate: verifica si una cadena está vacía
        Predicate<String> esVacio = s -> s.isEmpty();
        System.out.println("¿Cadena vacía?: " + esVacio.test("")); // true

        // Ejemplo de Consumer: imprime una cadena en consola
        Consumer<String> imprimir = s -> System.out.println(s);
        imprimir.accept("Hola mundo");

        // Ejemplo de Supplier: genera un número aleatorio
        Supplier<Double> aleatorio = () -> Math.random();
        System.out.println("Número aleatorio: " + aleatorio.get());

        // Ejemplo de Function: obtiene la longitud de una cadena
        Function<String, Integer> longitud = s -> s.length();
        System.out.println("Longitud de 'Copilot': " + longitud.apply("Copilot"));
    }
	
	//Crea un metodo que convierta numeros a una lista de cadenas 
	private static List<String> pruebaStrem(List<Integer> numeros) {
		
		List<String> listIntegers = new ArrayList<>();
		List<String> listIntegersNuevo = new ArrayList<>();
		listIntegers = numeros.stream().map(String::valueOf).toList();
		System.out.println("Conversion de Integer a cadena"+ listIntegers);
		
		listIntegers.forEach(n -> listIntegersNuevo.add(n));
		System.out.println("Nueva lista "+listIntegersNuevo );
		return listIntegers;
	}

	
	
}
