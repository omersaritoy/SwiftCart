package com.cavcav.swiftcart.auth.dto;

import com.cavcav.swiftcart.user.model.User;

public record UserRegisteredEvent(User user) {}