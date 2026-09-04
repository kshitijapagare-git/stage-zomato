package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.CategoryRequest;
import com.stagezomato.ecommerce.entity.Category;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.entity.ProductStatus;
import com.stagezomato.ecommerce.repository.CategoryRepository;
import com.stagezomato.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
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
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private CategoryRequest sampleRequest() {
        return new CategoryRequest("Electronics", "Electronic devices");
    }

    @Test
    void createGetUpdateDeleteCategory() throws Exception {
        String createResponse = mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Electronics")))
                .andExpect(jsonPath("$.description", is("Electronic devices")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Electronics")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(id.intValue())))
                .andExpect(jsonPath("$.totalElements", is(1)));

        CategoryRequest updateRequest = new CategoryRequest("Consumer Electronics", "Updated description");
        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Consumer Electronics")))
                .andExpect(jsonPath("$.description", is("Updated description")));

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategoryWithBlankNameReturnsBadRequest() throws Exception {
        CategoryRequest invalid = new CategoryRequest("", "Some description");
        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategoryWithDuplicateNameReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategoryReferencedByProductReturnsConflict() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name("Books")
                .description("Book category")
                .build());

        Product product = Product.builder()
                .name("Novel")
                .sku("SKU-BOOK-01")
                .price(new BigDecimal("9.99"))
                .stock(10)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .description("A great novel")
                .build();
        productRepository.save(product);

        mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isOk());
    }
}
