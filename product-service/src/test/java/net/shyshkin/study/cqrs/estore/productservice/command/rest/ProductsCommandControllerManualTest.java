package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@DisplayName("Start axon-server docker-compose file first")
@Disabled("Only for manual testing")
class ProductsCommandControllerManualTest {

    MockMvc mockMvc;

    @Autowired
    ProductsCommandController controller;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository repository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createProduct_correct() throws Exception {

        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(Faker.instance().commerce().productName())
                .price(new BigDecimal("125.00"))
                .quantity(2)
                .build();
        String jsonPayload = objectMapper.writeValueAsString(createProductRestModel);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/products")
                .contentType(APPLICATION_JSON)
                .content(jsonPayload))

                //then
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("Http POST: ")))
                .andReturn();

        String productIdString = mvcResult.getResponse()
                .getContentAsString()
                .replace("Http POST: ", "");

        UUID productId = UUID.fromString(productIdString);

        await()
                .timeout(1L, SECONDS)
                .untilAsserted(() -> {
                    Optional<ProductEntity> productOptional = repository.findByProductId(productId);
                    assertThat(productOptional)
                            .hasValueSatisfying(productEntity -> assertThat(productEntity)
                                    .hasNoNullFieldsOrProperties()
                                    .isEqualToIgnoringGivenFields(createProductRestModel, "productId")
                                    .hasFieldOrPropertyWithValue("productId", productId));
                });
    }

    @ParameterizedTest
    @Disabled("Because of bean validation added to the project - logic is changed")
    @CsvSource({
            ",125.0,Http POST: Title can not be empty",
            "iPhone 3,-125.0,Http POST: Price can not be less or equal to zero"
    })
    void createProduct_validationFailed(String title, String price, String expectedErrorMessage) throws Exception {

        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();
        String jsonPayload = objectMapper.writeValueAsString(createProductRestModel);

        //when
        mockMvc.perform(post("/products")
                .contentType(APPLICATION_JSON)
                .content(jsonPayload))

                //then
                .andExpect(status().isOk())
                .andExpect(content().string(expectedErrorMessage))
        ;
    }
}