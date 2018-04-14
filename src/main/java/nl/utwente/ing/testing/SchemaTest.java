package nl.utwente.ing.testing;

import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static nl.utwente.ing.testing.InitialSystemTest.getNewSessionID;


public class SchemaTest {

    public static String sessionID;


    @BeforeClass
    public static void getSessionID() {
        sessionID = getNewSessionID();
    }

    @Test
    public void test_GET_transactions_JSON_schema() {

        when().
                get("/api/v1/transactions?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/Transaction.json"));

    }

    @Test
    public void test_POST_transactions_JSON_schema() {

        when().
                post("/api/v1/transactions?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/Transaction.json"));

    }

}
