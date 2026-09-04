package com.stagezomato.ecommerce.controller;

import tools.jackson.databind.ObjectMapper;
import com.stagezomato.ecommerce.dto.OrderItemRequest;
import com.stagezomato.ecommerce.entity.Category;
import com.stagezomato.ecommerce.entity.Order;
import com.stagezomato.ecommerce.entity.OrderStatus;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.entity.ProductStatus;
import com.stagezomato.ecommerce.repository.CategoryRepository;
import com.stagezomato.ecommerce.repository.OrderRepository;
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
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long productId;

    private Long orderId;

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

        Order order = Order.builder()
                .customerName("Jane Doe")
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("49.99"))
                .status(OrderStatus.PENDING)
                .build();
        orderId = orderRepository.save(order).getId();
    }

    @Test
    void createGetUpdateDeleteOrderItem() throws Exception {
        OrderItemRequest createRequest = new OrderItemRequest(orderId, productId, 2, new BigDecimal("49.99"));

        String createResponse = mockMvc.perform(post("/order-items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(orderId.intValue())))
                .andExpect(jsonPath("$.productId", is(productId.intValue())))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/order-items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(2)));

        mockMvc.perform(get("/order-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemId.intValue())));

        OrderItemRequest updateRequest = new OrderItemRequest(orderId, productId, 5, new BigDecimal("45.00"));
        mockMvc.perform(put("/order-items/{id}", itemId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.unitPrice", is(45.00)));

        mockMvc.perform(delete("/order-items/{id}", itemId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/order-items/{id}", itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrderItemWithUnknownOrderReturnsNotFound() throws Exception {
        OrderItemRequest request = new OrderItemRequest(999999L, productId, 1, new BigDecimal("10.00"));
        mockMvc.perform(post("/order-items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrderItemWithUnknownProductReturnsNotFound() throws Exception {
        OrderItemRequest request = new OrderItemRequest(orderId, 999999L, 1, new BigDecimal("10.00"));
        mockMvc.perform(post("/order-items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDuplicateOrderItemForSameOrderAndProductReturnsConflict() throws Exception {
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 1, new BigDecimal("10.00"));

        mockMvc.perform(post("/order-items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/order-items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
