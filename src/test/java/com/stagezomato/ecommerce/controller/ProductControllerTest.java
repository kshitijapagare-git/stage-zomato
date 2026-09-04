package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.ProductRequest;
import com.stagezomato.ecommerce.entity.Category;
import com.stagezomato.ecommerce.repository.CategoryRepository;
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
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder()
                .name("Peripherals")
                .description("Peripheral devices")
                .build());
        categoryId = category.getId();
    }

    private ProductRequest sampleRequest() {
        return new ProductRequest("Wireless Mouse", "SKU-001", new BigDecimal("19.99"), 100, null, categoryId, "A wireless mouse");
    }

    @Test
    void createGetUpdateDeleteProduct() throws Exception {
        String createResponse = mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Wireless Mouse")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.categoryId", is(categoryId.intValue())))
                .andExpect(jsonPath("$.description", is("A wireless mouse")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-001")));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(id.intValue())));

        ProductRequest updateRequest = new ProductRequest("Wireless Mouse Pro", "SKU-001", new BigDecimal("29.99"), 50, null, categoryId, "An upgraded wireless mouse");
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Wireless Mouse Pro")))
                .andExpect(jsonPath("$.stock", is(50)))
                .andExpect(jsonPath("$.description", is("An upgraded wireless mouse")));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProductWithBlankNameReturnsBadRequest() throws Exception {
        ProductRequest invalid = new ProductRequest("", "SKU-002", new BigDecimal("10.00"), 10, null, categoryId, "Some description");
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithUnknownCategoryReturnsNotFound() throws Exception {
        ProductRequest request = new ProductRequest("Mechanical Keyboard", "SKU-003", new BigDecimal("59.99"), 20, null, 999999L, "A mechanical keyboard");
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProductWithBlankDescriptionReturnsBadRequest() throws Exception {
        ProductRequest invalid = new ProductRequest("Trackpad", "SKU-004", new BigDecimal("15.00"), 5, null, categoryId, "");
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
