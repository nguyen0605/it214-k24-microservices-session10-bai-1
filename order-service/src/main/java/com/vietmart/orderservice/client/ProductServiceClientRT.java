package com.vietmart.orderservice.client;

import com.vietmart.orderservice.dto.ProductInfo;
import com.vietmart.orderservice.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ProductServiceClientRT {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClientRT.class);
    private final RestTemplate restTemplate;

    @Autowired
    public ProductServiceClientRT(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductInfo getById(Long productId) {
        String url = "http://product-service/api/products/{id}";
        try {
            return restTemplate.getForObject(url, ProductInfo.class, productId);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Product not found with id: {}", productId);
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        } catch (ResourceAccessException e) {
            log.error("Timeout calling product-service for product id: {}. Message: {}", productId, e.getMessage());
            return getFallbackProduct(productId);
        } catch (Exception e) {
            log.error("Unexpected error fetching product id: {}. Message: {}", productId, e.getMessage());
            return getFallbackProduct(productId);
        }
    }

    private ProductInfo getFallbackProduct(Long productId) {
        log.warn("Executing fallback for product id: {}", productId);
        return new ProductInfo(productId, "Sản phẩm tạm thời không khả dụng", 0.0);
    }
}