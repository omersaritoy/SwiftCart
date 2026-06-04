package com.cavcav.swiftcart.notfication.listener;

import com.cavcav.swiftcart.auth.dto.UserRegisteredEvent;
import com.cavcav.swiftcart.notfication.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationListener {

    private final EmailVerificationService emailVerificationService;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("UserRegisteredEvent received: userId={}", event.user().getId());
        emailVerificationService.sendVerificationEmail(event.user());
    }
}