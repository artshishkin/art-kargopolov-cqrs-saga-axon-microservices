package net.shyshkin.study.cqrs.estore.productservice.command.interceptors;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.model.ProductIdDto;
import net.shyshkin.study.cqrs.estore.productservice.command.rest.CreateProductRestModel;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractAxonServerTest;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductLookupEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.errorhandling.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CreateProductCommandInterceptorTest extends AbstractAxonServerTest {

    @Test
    void createProduct_validationFailedInInterceptor() throws Exception {

        //given
        String title = "Title with incorrect ` symbol";
        String price = "666.00";
        String errorMessage = "Title must not contain ` symbol (fake validation)";

        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();

        //when
        ResponseEntity<ErrorMessage> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        ErrorMessage.class);

        //then
        ErrorMessage body = responseEntity.getBody();
        log.debug("Response body: {}", body);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body)
                .hasNoNullFieldsOrProperties()
                .satisfies(errorMess -> assertThat(errorMess.getTimestamp()).isEqualToIgnoringNanos(LocalDateTime.now()))
                .satisfies(errorMess -> assertThat(errorMess.getMessage()).contains(errorMessage));
    }

    @Test
    void createProduct_alreadyExists() {

        //given
        String title = getTitleOfExistingProduct();
        String price = "666.00";
        String errorMessage = "already exists";
        String errorPattern = CreateProductCommandInterceptor.PRODUCT_EXISTS_PATTERN;

        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();

        //when
        ResponseEntity<ErrorMessage> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        ErrorMessage.class);

        //then
        ErrorMessage body = responseEntity.getBody();
        log.debug("Response body: {}", body);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body)
                .hasNoNullFieldsOrProperties()
                .satisfies(errorMess -> assertThat(errorMess.getTimestamp()).isEqualToIgnoringNanos(LocalDateTime.now()))
                .satisfies(errorMess -> assertThat(errorMess.getMessage()).contains(errorMessage));
    }

    private String getTitleOfExistingProduct() {

        return lookupRepository
                .findAll(PageRequest.of(0, 1))
                .get()
                .findAny()
                .map(ProductLookupEntity::getTitle)
                .orElseGet(this::postNewCreateProductRestModel);
    }

    private String postNewCreateProductRestModel() {

        //given
        String title = Faker.instance().commerce().productName();
        String price = "666.00";

        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();

        //when
        ResponseEntity<ProductIdDto> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        ProductIdDto.class);

        //then
        ProductIdDto productIdDto = responseEntity.getBody();
        log.debug("Response body: {}", productIdDto);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UUID productId = productIdDto.getProductId();
        assertThat(productId).isNotNull();

        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        return title;
    }
}