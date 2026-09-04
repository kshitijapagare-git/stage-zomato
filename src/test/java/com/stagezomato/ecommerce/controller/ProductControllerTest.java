package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.ProductRequest;
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

    private ProductRequest sampleRequest() {
        return new ProductRequest("Wireless Mouse", "SKU-001", new BigDecimal("19.99"), 100, null);
    }

    @Test
    void createGetUpdateDeleteProduct() throws Exception {
        String createResponse = mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Wireless Mouse")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-001")));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(id.intValue())));

        ProductRequest updateRequest = new ProductRequest("Wireless Mouse Pro", "SKU-001", new BigDecimal("29.99"), 50, null);
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Wireless Mouse Pro")))
                .andExpect(jsonPath("$.stock", is(50)));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProductWithBlankNameReturnsBadRequest() throws Exception {
        ProductRequest invalid = new ProductRequest("", "SKU-002", new BigDecimal("10.00"), 10, null);
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
