package nl.utwente.ing.testing;

import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;


public class SchemaTest {

    public static String sessionID;


    @BeforeClass
    public static void getSessionID() {
        sessionID = get("/api/v1/sessions").asString();
        System.out.println(sessionID);
    }

    @Test
    public void test_GET_transactions_JSON_schema() {

        when().
                get("/api/v1/transactions?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("Transaction.json"));

    }

    @Test
    public void test_POST_transactions_JSON_schema() {

        when().
                post("/api/v1/transactions?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("Transaction.json"));

    }

}
