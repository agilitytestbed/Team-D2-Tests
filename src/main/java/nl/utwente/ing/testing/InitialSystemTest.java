package nl.utwente.ing.testing;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;

public class InitialSystemTest {

    private static String sessionID;

    @BeforeAll
    public static void setup() {
        // Get sessionID
        String responseString = when().post("/api/v1/sessions").
                then().contentType(ContentType.JSON).
                extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        sessionID = (String) responseMap.get("id");
    }

    @Test
    public void testSessionIDRetrieval() {
        when().post("/api/v1/sessions").
                then().statusCode(201).
                body("$", hasKey("id"));
    }


}