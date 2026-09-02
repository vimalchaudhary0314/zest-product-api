package com.zestindia.productapi.product;

import com.zestindia.productapi.product.ProductDtos.*;

import java.util.List;

public final class ProductMapper {
    private ProductMapper() {}

    public static ProductResponse toResponse(Product p) {
        List<ItemResponse> items = p.getItems().stream()
                .map(i -> new ItemResponse(i.getId(), i.getQuantity()))
                .toList();

        return new ProductResponse(
                p.getId(),
                p.getProductName(),
                p.getCreatedBy(),
                p.getCreatedOn(),
                p.getModifiedBy(),
                p.getModifiedOn(),
                items);
    }
}
