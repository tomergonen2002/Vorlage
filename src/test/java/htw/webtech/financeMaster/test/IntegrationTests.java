package htw.webtech.financeMaster.test;

import htw.webtech.financeMaster.rest.RestServiceApplication;
import htw.webtech.financeMaster.rest.dto.CreateCategoryRequest;
import htw.webtech.financeMaster.rest.dto.CreateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RestServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createCategoryAndTransaction_thenGetTransactions() {
        // Create a category
        CreateCategoryRequest catReq = new CreateCategoryRequest();
        catReq.setName("IntegrationCat");
        catReq.setDescription("From integration test");

    ResponseEntity<Map<String, Object>> catRes = restTemplate.exchange(
        "/categories",
        HttpMethod.POST,
        new HttpEntity<>(catReq),
        new ParameterizedTypeReference<Map<String, Object>>() {}
    );
    assertThat(catRes.getStatusCode().is2xxSuccessful()).isTrue();
    Map<String, Object> body = catRes.getBody();
    assertThat(body).isNotNull();
    Object catIdObj = body.get("id");
    assertThat(catIdObj).isNotNull();
    Long catId = ((Number)catIdObj).longValue();

        // Create a transaction for that category
        CreateTransactionRequest txReq = new CreateTransactionRequest();
        txReq.setType("Einnahme");
        txReq.setAmount(12.34);
        txReq.setDescription("Test payment");
        txReq.setDate("2025-11-02");
        txReq.setCategoryId(catId);

    ResponseEntity<Map<String, Object>> txRes = restTemplate.exchange(
        "/transactions",
        HttpMethod.POST,
        new HttpEntity<>(txReq),
        new ParameterizedTypeReference<Map<String, Object>>() {}
    );
    assertThat(txRes.getStatusCode().is2xxSuccessful()).isTrue();
    Map<String, Object> txBody = txRes.getBody();
    assertThat(txBody).isNotNull();
    assertThat(((Number)txBody.get("amount")).doubleValue()).isEqualTo(12.34);

        // GET transactions and verify at least one exists
    ResponseEntity<Map<String, Object>[]> getRes = restTemplate.exchange(
        "/transactions",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<Map<String, Object>[]>() {}
    );
    assertThat(getRes.getStatusCode().is2xxSuccessful()).isTrue();
    Map<String, Object>[] arr = getRes.getBody();
    assertThat(arr).isNotNull();
    assertThat(arr.length).isGreaterThanOrEqualTo(1);
    }
}
