package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import net.shyshkin.study.cqrs.estore.productservice.testcontainers.AxonServerContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

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
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "axon.axonserver.servers=${AXON_SERVERS}"
})
@Testcontainers
class ProductsCommandControllerTest {

    MockMvc mockMvc;

    @Container
    public static AxonServerContainer axonServer = AxonServerContainer.getInstance();

    @Autowired
    ProductsCommandController controller;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository repository;

    @Autowired
    WebApplicationContext context;

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

        String productId = mvcResult.getResponse()
                .getContentAsString()
                .replace("Http POST: ", "");

        log.debug("Product Id: {}", productId);

        await()
                .timeout(1L, SECONDS)
                .untilAsserted(() -> {
                    Optional<ProductEntity> productOptional = repository.findByProductId(productId);
                    assertThat(productOptional)
                            .hasValueSatisfying(productEntity -> assertThat(productEntity)
                                    .hasNoNullFieldsOrProperties()
                                    .isEqualToIgnoringGivenFields(createProductRestModel, "productId")
                                    .hasFieldOrPropertyWithValue("productId", productId)
                                    .satisfies(entity -> log.debug("Entity: {}", entity))
                            );
                });
    }

    @ParameterizedTest
    @CsvSource({
            ",125.0,NotBlank.title,Product title is a required field",
            "iPhone 3,-125.0,Min.price,Price can not be lower then 1"
    })
    void createProduct_validationFailed(String title, String price, String errorCode, String defaultMessage) throws Exception {

        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();
        String jsonPayload = objectMapper.writeValueAsString(createProductRestModel);
        log.debug("JSON request body: {}", jsonPayload);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/products")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload))

                //then
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(MethodArgumentNotValidException.class)
                                .hasMessageContaining("Validation failed for argument [0] in public")
                                .hasMessageContaining(errorCode)
                                .hasMessageContaining("default message [" + defaultMessage))
                .andReturn();

        String contentAsString = mvcResult.getResponse()
                .getContentAsString();

        log.debug("Response: {}", contentAsString);
    }
}