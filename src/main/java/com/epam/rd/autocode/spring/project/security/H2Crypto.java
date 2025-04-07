package com.epam.rd.autocode.spring.project.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Slf4j
public class H2Crypto {
    public static String genSalt(String type) {
        log.debug("Generating salt using type: {}", type);
        return BCrypt.gensalt();
    }

    public static String crypt(String password, String salt) {
        log.debug("Hashing password using provided salt");
        return BCrypt.hashpw(password, salt);
    }
}