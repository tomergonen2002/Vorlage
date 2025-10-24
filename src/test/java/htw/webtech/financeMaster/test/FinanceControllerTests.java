package htw.webtech.financeMaster.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = htw.webtech.financeMaster.rest.RestServiceApplication.class)
@AutoConfigureMockMvc
class FinanceControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Test
    void guestLoginCreatesDefaultCategories() throws Exception {
        // login guest
        var body = om.createObjectNode();
        body.put("email", "guest@finance.local");
        body.put("passwordHash", "");
        var res = mvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();

        var user = om.readTree(res.getResponse().getContentAsString());
        long userId = user.get("id").asLong();

        // categories should have at least the two defaults
        mvc.perform(get("/categories").param("userId", String.valueOf(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name=='Lebensmittel')]").exists())
            .andExpect(jsonPath("$[?(@.name=='Gehalt')]").exists());
    }

    @Test
    void createTransactionWithCategoryId() throws Exception {
        // login guest to ensure defaults
        var body = om.createObjectNode();
        body.put("email", "guest@finance.local");
        body.put("passwordHash", "");
        var res = mvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();
        var user = om.readTree(res.getResponse().getContentAsString());
        long userId = user.get("id").asLong();

        // fetch categories to get an id
        var catRes = mvc.perform(get("/categories").param("userId", String.valueOf(userId)))
            .andExpect(status().isOk())
            .andReturn();
        var cats = om.readTree(catRes.getResponse().getContentAsString());
        long catId = cats.get(0).get("id").asLong();

        // create tx
        var tx = om.createObjectNode();
        tx.put("type", "Einnahme");
        tx.put("amount", 10.5);
        tx.put("description", "Test");
        tx.put("date", java.time.LocalDate.now().toString());
        tx.put("categoryId", catId);

        mvc.perform(post("/transactions").param("userId", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(tx)))
            .andExpect(status().isCreated());

        // verify listed
        mvc.perform(get("/transactions").param("userId", String.valueOf(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("Einnahme"));
    }
}
