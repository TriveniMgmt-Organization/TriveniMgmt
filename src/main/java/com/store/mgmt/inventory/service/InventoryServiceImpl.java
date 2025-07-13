package com.store.mgmt.inventory.service;

import com.store.mgmt.inventory.model.entity.Product;
import com.store.mgmt.inventory.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;

    public InventoryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void checkStock(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (product.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
        }
    }

    @Override
    public void updateStock(UUID productId, Integer quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock cannot be negative");
        }
        product.setQuantity(newQuantity);
        productRepository.save(product);
    }

    @Override
    public BigDecimal getProductPrice(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return product.getPrice();
    }
}