package com.zestindia.productapi.product;

import com.zestindia.productapi.product.ProductDtos.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductAuditService auditService;

    @InjectMocks ProductService productService;

    @Test
    void create_shouldPersistProduct() {
        ProductRequest request = new ProductRequest(
                "Laptop",
                List.of(new ItemRequest(5)));

        Product saved = Product.builder()
                .id(1L)
                .productName("Laptop")
                .createdBy("user@example.com")
                .createdOn(java.time.Instant.now())
                .build();
        saved.getItems().add(Item.builder().id(10L).product(saved).quantity(5).build());

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request, "user@example.com");

        assertEquals(1L, response.id());
        assertEquals("Laptop", response.productName());
        assertEquals(1, response.items().size());
        verify(productRepository).save(any(Product.class));
        verify(auditService).audit("CREATE", 1L, "user@example.com");
    }

    @Test
    void findById_shouldThrowWhenMissing() {
        when(productRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.findById(99L));
    }

    @Test
    void delete_shouldDeleteProduct() {
        Product product = Product.builder().id(1L).productName("Phone").build();
        when(productRepository.findByIdWithItems(1L)).thenReturn(Optional.of(product));

        productService.delete(1L, "admin@example.com");

        verify(productRepository).delete(product);
        verify(auditService).audit("DELETE", 1L, "admin@example.com");
    }
}
