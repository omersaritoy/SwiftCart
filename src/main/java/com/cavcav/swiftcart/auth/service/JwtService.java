package com.cavcav.swiftcart.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(String email){
        Map<String, Object> claims = new HashMap<String, Object>();
        return createToken(claims,email);
    }
}
