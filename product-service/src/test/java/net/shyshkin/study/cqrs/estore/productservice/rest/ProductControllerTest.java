package net.shyshkin.study.cqrs.estore.productservice.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
        "axon.axonserver.enabled=false"
})
class ProductControllerTest {

    MockMvc mockMvc;

    @Autowired
    ProductController controller;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createProduct() throws Exception {

        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title("iPhone 3")
                .price(new BigDecimal("125.0"))
                .quantity(2)
                .build();
        String jsonPayload = objectMapper.writeValueAsString(createProductRestModel);

        //when
        mockMvc.perform(post("/products")
                .contentType(APPLICATION_JSON)
                .content(jsonPayload))

                //then
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("Http POST: CreateProductCommand(productId=")))
                .andExpect(content().string(endsWith(", title=iPhone 3, price=125.0, quantity=2)")))
        ;
    }
}