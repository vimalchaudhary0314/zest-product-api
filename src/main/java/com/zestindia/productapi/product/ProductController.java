package com.zestindia.productapi.product;

import com.zestindia.productapi.product.ProductDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products with pagination")
    public Page<ProductResponse> getAll(
            @PageableDefault(size = 10, sort = "createdOn") Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Create product")
    public ProductResponse create(@Valid @RequestBody ProductRequest request,
                                  Authentication authentication) {
        return productService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Update product")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ProductRequest request,
                                  Authentication authentication) {
        return productService.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product - ADMIN only")
    public void delete(@PathVariable Long id, Authentication authentication) {
        productService.delete(id, authentication.getName());
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get items for a product")
    public List<ItemResponse> getItems(@PathVariable Long id) {
        return productService.findItems(id);
    }
}
