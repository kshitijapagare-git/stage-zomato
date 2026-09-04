package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.OrderRequest;
import com.stagezomato.ecommerce.entity.Category;
import com.stagezomato.ecommerce.entity.OrderStatus;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.entity.ProductStatus;
import com.stagezomato.ecommerce.repository.CategoryRepository;
import com.stagezomato.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder()
                .name("Keyboards")
                .description("Keyboard category")
                .build());

        Product product = Product.builder()
                .name("Keyboard")
                .sku("SKU-KB-01")
                .price(new BigDecimal("49.99"))
                .stock(200)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .description("A mechanical keyboard")
                .build();
        productId = productRepository.save(product).getId();
    }

    @Test
    void createGetUpdateDeleteOrder() throws Exception {
        OrderRequest createRequest = new OrderRequest("Jane Doe", productId, 2, new BigDecimal("49.99"), null);

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName", is("Jane Doe")))
                .andExpect(jsonPath("$.productId", is(productId.intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(2)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(orderId.intValue())));

        OrderRequest updateRequest = new OrderRequest("Jane Doe", productId, 5, new BigDecimal("45.00"), OrderStatus.CONFIRMED);
        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrderWithUnknownProductReturnsNotFound() throws Exception {
        OrderRequest request = new OrderRequest("John Doe", 999999L, 1, new BigDecimal("10.00"), null);
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
