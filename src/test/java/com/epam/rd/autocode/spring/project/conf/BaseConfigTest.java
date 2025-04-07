package com.epam.rd.autocode.spring.project.conf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseConfigTest {

    @Test
    @DisplayName("Annotation [Configuration] exist for BaseConfig class")
    public void testsConfigAnnotation() throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.epam.rd.autocode.spring.project.conf.BaseConfig");

        assertTrue(aClass.isAnnotationPresent(Configuration.class),
                String.format("Class [%s]. [@Configuration] is missed.", aClass.getSimpleName()));
    }
}
