package com.vietmart.orderservice.client;

import com.vietmart.orderservice.dto.ProductInfo;
import com.vietmart.orderservice.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceClientRTTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceClientRT client;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetById_Success() {
        Long productId = 1L;
        ProductInfo expectedProduct = new ProductInfo(productId, "Sản Phẩm Thử Nghiệm", 150000.0);
        String url = "http://product-service/api/products/{id}";

        when(restTemplate.getForObject(url, ProductInfo.class, productId))
                .thenReturn(expectedProduct);

        ProductInfo actualProduct = client.getById(productId);

        assertNotNull(actualProduct);
        assertEquals(productId, actualProduct.getId());
        assertEquals("Sản Phẩm Thử Nghiệm", actualProduct.getName());
        assertEquals(150000.0, actualProduct.getPrice());
        verify(restTemplate, times(1)).getForObject(url, ProductInfo.class, productId);
    }

    @Test
    public void testGetById_Timeout_ReturnsFallback() {
        Long productId = 2L;
        String url = "http://product-service/api/products/{id}";

        when(restTemplate.getForObject(url, ProductInfo.class, productId))
                .thenThrow(new ResourceAccessException("Read timed out"));

        ProductInfo fallbackProduct = client.getById(productId);

        assertNotNull(fallbackProduct);
        assertEquals(productId, fallbackProduct.getId());
        assertEquals("Sản phẩm tạm thời không khả dụng", fallbackProduct.getName());
        assertEquals(0.0, fallbackProduct.getPrice());
    }

    @Test
    public void testGetById_NotFound_ThrowsProductNotFoundException() {
        Long productId = 3L;
        String url = "http://product-service/api/products/{id}";

        when(restTemplate.getForObject(url, ProductInfo.class, productId))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThrows(ProductNotFoundException.class, () -> {
            client.getById(productId);
        });
    }
}