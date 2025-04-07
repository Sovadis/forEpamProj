package com.epam.rd.autocode.spring.project.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgeGroupModelTest {

    private static List<?> constants;
    private static Class<?> clazz;

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        clazz = Class.forName(Constants.AGE_GROUP_TYPE);
        constants = Arrays.asList(clazz.getEnumConstants());
    }

    @Test
    @DisplayName("Class " + Constants.AGE_GROUP_TYPE + " is Enum")
    void checkIsEnum() {
        final var actual = clazz.isEnum();
        assertTrue(actual);
    }

    @Test
    @DisplayName("There must be " + Constants.AgeGroup.ENUM_COUNT_CONSTANTS + " constants")
    void checkCountConstants() {
        var actual = constants.size();
        assertEquals(Constants.AgeGroup.ENUM_COUNT_CONSTANTS, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.AgeGroup.ENUM_CONSTANT_CHILD)
    void checkEnumConstantChild() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.AgeGroup.ENUM_CONSTANT_CHILD))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.AgeGroup.ENUM_CONSTANT_TEEN)
    void checkEnumConstantTeen() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.AgeGroup.ENUM_CONSTANT_TEEN))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.AgeGroup.ENUM_CONSTANT_ADULT)
    void checkEnumConstantAdult() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.AgeGroup.ENUM_CONSTANT_ADULT))
                .count();
        assertEquals(1, actual);
    }

    @Test
    @DisplayName("There must be constant with name " + Constants.AgeGroup.ENUM_CONSTANT_OTHER)
    void checkEnumConstantOther() {
        var actual = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> name.equals(Constants.AgeGroup.ENUM_CONSTANT_OTHER))
                .count();
        assertEquals(1, actual);
    }
}
