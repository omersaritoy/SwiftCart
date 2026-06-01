package com.cavcav.swiftcart.auth.service;


import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user= repository.findByEmail(email).orElseThrow(()->new BusinessException("User not found","USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        return new UserPrincipal(user);
    }
}
