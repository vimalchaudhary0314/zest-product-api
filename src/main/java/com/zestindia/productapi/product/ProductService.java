package com.zestindia.productapi.product;

import com.zestindia.productapi.product.ProductDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductAuditService auditService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductMapper.toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request, String username) {
        Instant now = Instant.now();

        Product product = Product.builder()
                .productName(request.productName())
                .createdBy(username)
                .createdOn(now)
                .build();

        applyItems(product, request.items());
        Product saved = productRepository.save(product);
        auditService.audit("CREATE", saved.getId(), username);
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request, String username) {
        Product product = getProduct(id);
        product.setProductName(request.productName());
        product.setModifiedBy(username);
        product.setModifiedOn(Instant.now());

        product.getItems().clear();
        applyItems(product, request.items());

        Product saved = productRepository.save(product);
        auditService.audit("UPDATE", saved.getId(), username);
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        Product product = getProduct(id);
        productRepository.delete(product);
        auditService.audit("DELETE", id, username);
    }

    @Transactional(readOnly = true)
    public java.util.List<ItemResponse> findItems(Long id) {
        return getProduct(id).getItems().stream()
                .map(i -> new ItemResponse(i.getId(), i.getQuantity()))
                .toList();
    }

    private Product getProduct(Long id) {
        return productRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void applyItems(Product product, java.util.List<ItemRequest> requests) {
        if (requests == null) return;
        requests.forEach(req -> product.getItems().add(
                Item.builder().product(product).quantity(req.quantity()).build()));
    }
}
