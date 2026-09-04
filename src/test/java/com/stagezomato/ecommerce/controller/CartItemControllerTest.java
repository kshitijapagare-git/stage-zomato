package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.CartItemRequest;
import com.stagezomato.ecommerce.entity.Cart;
import com.stagezomato.ecommerce.entity.CartStatus;
import com.stagezomato.ecommerce.entity.Category;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.entity.ProductStatus;
import com.stagezomato.ecommerce.repository.CartRepository;
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
class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    private Long productId;

    private Long secondProductId;

    private Long cartId;

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

        Product secondProduct = Product.builder()
                .name("Mouse")
                .sku("SKU-MS-01")
                .price(new BigDecimal("19.99"))
                .stock(200)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .description("A wireless mouse")
                .build();
        secondProductId = productRepository.save(secondProduct).getId();

        Cart cart = cartRepository.save(Cart.builder()
                .customerName("Jane Doe")
                .status(CartStatus.ACTIVE)
                .build());
        cartId = cart.getId();
    }

    @Test
    void createGetUpdateDeleteCartItem() throws Exception {
        CartItemRequest createRequest = new CartItemRequest(cartId, productId, 2);

        String createResponse = mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId", is(cartId.intValue())))
                .andExpect(jsonPath("$.productId", is(productId.intValue())))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/carts/{cartId}/items/{itemId}", cartId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(2)));

        mockMvc.perform(get("/carts/{cartId}/items", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemId.intValue())));

        CartItemRequest updateRequest = new CartItemRequest(cartId, productId, 5);
        mockMvc.perform(put("/carts/{cartId}/items/{itemId}", cartId, itemId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));

        mockMvc.perform(delete("/carts/{cartId}/items/{itemId}", cartId, itemId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/carts/{cartId}/items/{itemId}", cartId, itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCartItemWithUnknownCartReturnsNotFound() throws Exception {
        CartItemRequest request = new CartItemRequest(999999L, productId, 1);
        mockMvc.perform(post("/carts/{cartId}/items", 999999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCartItemWithUnknownProductReturnsNotFound() throws Exception {
        CartItemRequest request = new CartItemRequest(cartId, 999999L, 1);
        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addingSameProductTwiceUpdatesQuantityInsteadOfConflict() throws Exception {
        CartItemRequest firstRequest = new CartItemRequest(cartId, productId, 1);

        String firstResponse = mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long firstItemId = objectMapper.readTree(firstResponse).get("id").asLong();

        CartItemRequest secondRequest = new CartItemRequest(cartId, productId, 4);
        String secondResponse = mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity", is(4)))
                .andReturn().getResponse().getContentAsString();

        Long secondItemId = objectMapper.readTree(secondResponse).get("id").asLong();

        org.junit.jupiter.api.Assertions.assertEquals(firstItemId, secondItemId);

        mockMvc.perform(get("/carts/{cartId}/items", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void updateCartItemToProductUsedByAnotherItemInSameCartReturnsConflict() throws Exception {
        CartItemRequest firstRequest = new CartItemRequest(cartId, productId, 1);
        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CartItemRequest secondRequest = new CartItemRequest(cartId, secondProductId, 1);
        String secondResponse = mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long secondItemId = objectMapper.readTree(secondResponse).get("id").asLong();

        CartItemRequest conflictingUpdate = new CartItemRequest(cartId, productId, 3);
        mockMvc.perform(put("/carts/{cartId}/items/{itemId}", cartId, secondItemId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(conflictingUpdate)))
                .andExpect(status().isConflict());
    }
}
