package com.zestindia.productapi.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ProductDtos {
    private ProductDtos() {}

    public record ItemRequest(
            @NotNull @PositiveOrZero Integer quantity) {}

    public record ProductRequest(
            @NotBlank @Size(max = 255) String productName,
            @Valid List<ItemRequest> items) {}

    public record ItemResponse(
            Long id,
            Integer quantity) {}

    public record ProductResponse(
            Long id,
            String productName,
            String createdBy,
            Instant createdOn,
            String modifiedBy,
            Instant modifiedOn,
            List<ItemResponse> items) {}
}
