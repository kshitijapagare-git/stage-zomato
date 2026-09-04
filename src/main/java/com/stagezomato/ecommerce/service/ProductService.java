package com.stagezomato.ecommerce.service;

import com.stagezomato.ecommerce.dto.ProductRequest;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.entity.ProductStatus;
import com.stagezomato.ecommerce.exception.ResourceNotFoundException;
import com.stagezomato.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku())
                .price(request.price())
                .stock(request.stock())
                .status(request.status() != null ? request.status() : ProductStatus.ACTIVE)
                .build();
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        product.setName(request.name());
        product.setSku(request.sku());
        product.setPrice(request.price());
        product.setStock(request.stock());
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = getById(id);
        productRepository.delete(product);
    }
}
