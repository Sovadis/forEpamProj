package com.epam.rd.autocode.spring.project.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LanguageModelTest {

    private static List<?> constants;
    private static Class<?> clazz;

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        clazz = Class.forName(Constants.LANGUAGE_TYPE);
        constants = Arrays.asList(clazz.getEnumConstants());
    }

    @Test
    @DisplayName("Class " + Constants.LANGUAGE_TYPE + " is Enum")
    void checkIsEnum() {
        final var actual = clazz.isEnum();
        assertTrue(actual);
    }

    @Test
    @DisplayName("There must be " + Constants.Language.ENUM_COUNT_CONSTANTS + " constants")
    void checkCountConstants() {
        var actual = constants.size();
        assertEquals(Constants.Language.ENUM_COUNT_CONSTANTS, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_ENGLISH)
    void checkEnumConstantEnglish() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_ENGLISH))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_GERMAN)
    void checkEnumConstantGerman() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_GERMAN))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_FRENCH)
    void checkEnumConstantFrench() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_FRENCH))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_SPANISH)
    void checkEnumConstantSpanish() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_SPANISH))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_JAPANESE)
    void checkEnumConstantJapanese() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_JAPANESE))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_UKRAINIAN)
    void checkEnumConstantUkrainian() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_UKRAINIAN))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.Language.ENUM_CONSTANT_OTHER)
    void checkEnumConstantOther() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.Language.ENUM_CONSTANT_OTHER))
                .count();
        assertEquals(1, actual);
    }
}
