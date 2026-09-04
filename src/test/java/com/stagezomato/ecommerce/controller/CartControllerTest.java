package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.CartRequest;
import com.stagezomato.ecommerce.entity.CartStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CartRequest sampleRequest() {
        return new CartRequest("Jane Doe", CartStatus.ACTIVE);
    }

    @Test
    void createGetUpdateDeleteCart() throws Exception {
        String createResponse = mockMvc.perform(post("/carts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName", is("Jane Doe")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/carts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Jane Doe")));

        mockMvc.perform(get("/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(id.intValue())));

        CartRequest updateRequest = new CartRequest("John Smith", CartStatus.INACTIVE);
        mockMvc.perform(put("/carts/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("John Smith")))
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        mockMvc.perform(delete("/carts/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/carts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCartWithBlankCustomerNameReturnsBadRequest() throws Exception {
        CartRequest invalid = new CartRequest("", CartStatus.ACTIVE);
        mockMvc.perform(post("/carts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCartWithMissingStatusReturnsBadRequest() throws Exception {
        CartRequest invalid = new CartRequest("Jane Doe", null);
        mockMvc.perform(post("/carts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownCartReturnsNotFound() throws Exception {
        mockMvc.perform(get("/carts/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUnknownCartReturnsNotFound() throws Exception {
        mockMvc.perform(put("/carts/{id}", 999999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownCartReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/carts/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}
