package com.zestindia.productapi.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestindia.productapi.product.ProductDtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProductService productService;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void create_shouldReturn201() throws Exception {
        ProductRequest request = new ProductRequest("Keyboard", List.of(new ItemRequest(2)));

        ProductResponse response = new ProductResponse(
                1L, "Keyboard", "user@example.com", Instant.now(),
                null, null, List.of(new ItemResponse(1L, 2)));

        when(productService.create(any(), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void create_shouldRejectInvalidPayload() throws Exception {
        ProductRequest request = new ProductRequest("", null);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
