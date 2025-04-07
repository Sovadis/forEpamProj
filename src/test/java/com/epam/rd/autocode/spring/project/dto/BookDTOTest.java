package com.epam.rd.autocode.spring.project.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookDTOTest {

    private static List<Field> allFields;
    private static List<Constructor<?>> allConstructors;

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(Constants.BOOK_DTO_TYPE);
        allFields = Arrays.asList(clazz.getDeclaredFields());
        allConstructors = Arrays.asList(clazz.getConstructors());
    }

    @Test
    @DisplayName("Count constructors")
    void checkCountConstructors() {
        assertEquals(Constants.BookDTO.CLASS_COUNT_CONSTRUCTORS, allConstructors.size());
    }

    @Test
    @DisplayName("Modifiers constructors can be public")
    void checkModifiersConstructors() {
        var actual = allConstructors.stream()
                .allMatch(constructor -> Modifier.isPublic(constructor.getModifiers()));

        assertTrue(actual);
    }

    @Test
    @DisplayName(Constants.BookDTO.CLASS_NAME + " has default constructor")
    void checkDefaultConstructor() {
        var count = allConstructors.stream()
                .filter(constructor -> constructor.getParameterCount() == 0)
                .count();

        assertEquals(1, count);
    }

    @Test
    @DisplayName(Constants.BookDTO.CLASS_NAME + " has constructor with " + Constants.BookDTO.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS + " parameters")
    void checkConstructorWithParameter() {
        var count = allConstructors.stream()
                .filter(constructor -> constructor.getParameterCount() == Constants.BookDTO.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS)
                .count();

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Check parameter type in constructor with parameter")
    void checkParameterTypeForConstructorWithParameter() {
        final var constructor = allConstructors.stream()
                .filter(c -> c.getParameterCount() == Constants.BookDTO.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No constructor with parameters"));

        final var parameters = Arrays.asList(constructor.getParameters());

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(Constants.STRING_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + Constants.STRING_TYPE));

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(Constants.AGE_GROUP_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + Constants.AGE_GROUP_TYPE));

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(Constants.BIG_DECIMAL_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + Constants.BIG_DECIMAL_TYPE));

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(Constants.LOCAL_DATE_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + Constants.LOCAL_DATE_TYPE));

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(Constants.INT_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + Constants.INT_TYPE));

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(Constants.LANGUAGE_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + Constants.LANGUAGE_TYPE));

        assertEquals(10, parameters.size());
    }

    @Test
    @DisplayName("Count fields")
    void checkCountFields() {
        assertEquals(Constants.BookDTO.CLASS_COUNT_FIELDS, allFields.size());
    }

    @Test
    @DisplayName("Modifiers fields can be private")
    void checkModifiersFields() {
        var count = allFields.stream()
                .filter(f -> Modifier.isPrivate(f.getModifiers()))
                .count();

        assertEquals(Constants.BookDTO.CLASS_COUNT_FIELDS, count);
    }

    @DisplayName("Check field type and field name")
    @ParameterizedTest
    @CsvSource({
            "java.lang.String, name, 1",
            "java.lang.String, genre, 1",
            "com.epam.rd.autocode.spring.project.model.enums.AgeGroup, ageGroup, 1",
            "java.math.BigDecimal, price, 1",
            "java.time.LocalDate, publicationDate, 1",
            "java.lang.String, author, 1",
            "java.lang.Integer, pages, 1",
            "java.lang.String, characteristics, 1",
            "java.lang.String, description, 1",
            "com.epam.rd.autocode.spring.project.model.enums.Language, language, 1"
    })
    void checkNameFieldType(String fieldType, String fieldName, long expected) {
        var count = allFields.stream()
                .filter(f -> f.getType().getTypeName().equals(fieldType)
                        & f.getName().equals(fieldName))
                .count();
        assertEquals(expected, count);
    }
}
