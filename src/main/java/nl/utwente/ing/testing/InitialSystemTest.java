package nl.utwente.ing.testing;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class InitialSystemTest {

    @Test
    public void testSessionIDRetrieval() {
        get("/api/v1/sessions").then().statusCode(200).body("isEmpty()", Matchers.is(false));
    }



}
