package nl.utwente.ing.testing;

import org.junit.Test;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.RestAssured.*;


public class SchemaTest {

    @Test
    public void test_GET_transactions_JSON_schema() {

        when().
                get("/api/v1/transactions").
                then().
                assertThat().body(matchesJsonSchemaInClasspath("TransactionList.json"));

    }

    @Test
    public void test_POST_transactions_JSON_schema() {

        when().
                post("/api/v1/transactions").
                then().
                assertThat().body(matchesJsonSchemaInClasspath("Transaction.json"));

    }

}
