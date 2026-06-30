package com.cavcav.swiftcart.product.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        @NotNull(message = "Stock is required")
        Integer stock
) {}
