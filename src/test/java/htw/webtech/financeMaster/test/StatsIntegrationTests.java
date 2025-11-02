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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = RestServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatsIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void statsAndRecent_areAvailableAfterCreatingData() {
        // create category
        CreateCategoryRequest catReq = new CreateCategoryRequest();
        catReq.setName("StatCat");
        catReq.setDescription("for stats");
        ResponseEntity<Map> catRes = restTemplate.postForEntity("/categories", catReq, Map.class);
        assertThat(catRes.getStatusCode().is2xxSuccessful()).isTrue();
        Map body = catRes.getBody();
        assertThat(body).isNotNull();
        Number catId = (Number) body.get("id");
        assertThat(catId).isNotNull();

        // create transaction
        CreateTransactionRequest txReq = new CreateTransactionRequest();
        txReq.setType("Einnahme");
        txReq.setAmount(99.9);
        txReq.setDescription("stat test");
        txReq.setDate("2025-11-02");
        txReq.setCategoryId(catId.longValue());
        ResponseEntity<Map> txRes = restTemplate.postForEntity("/transactions", txReq, Map.class);
        assertThat(txRes.getStatusCode().is2xxSuccessful()).isTrue();

        // call stats
        ResponseEntity<Map> statsRes = restTemplate.exchange("/stats", HttpMethod.GET, null, new ParameterizedTypeReference<Map>() {});
        assertThat(statsRes.getStatusCode().is2xxSuccessful()).isTrue();
        Map stats = statsRes.getBody();
        assertThat(stats).isNotNull();
        assertThat(((Number)stats.get("totalTransactions")).intValue()).isGreaterThanOrEqualTo(1);

        // call recent
        ResponseEntity(List.class) recentRes = restTemplate.exchange("/transactions/recent", HttpMethod.GET, null, List.class);
        assertThat(recentRes.getStatusCode().is2xxSuccessful()).isTrue();
        List recent = recentRes.getBody();
        assertThat(recent).isNotNull();
        assertThat(recent.size()).isGreaterThanOrEqualTo(1);
    }
}
