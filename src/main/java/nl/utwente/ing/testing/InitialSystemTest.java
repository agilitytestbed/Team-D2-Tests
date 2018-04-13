package nl.utwente.ing.testing;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import nl.utwente.ing.testing.bean.Category;
import nl.utwente.ing.testing.bean.Transaction;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class InitialSystemTest {

    private static String sessionID;

    @BeforeAll
    public static void setup() {
        sessionID = getNewSessionID();
    }

    @Test
    public void testSessionIDRetrieval() {
        // Test status code and that body contains id field
        when().post("/api/v1/sessions").
                then().statusCode(201).
                body("$", hasKey("id"));
    }

    @Test
    public void testGetTransactions() {
        // Test invalid session ID status code
        when().get("/api/v1/transactions").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get("/api/v1/transactions").then().statusCode(401);

        // Test responses and status codes
        String newSessionID = getNewSessionID();
        ArrayList<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.000Z",
                100, "NL01RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2016-04-13T08:06:10.000Z",
                200, "NL02RABO0300065264", "withdrawal"));
        transactionList.add(new Transaction("2017-04-13T08:06:10.000Z",
                300, "NL03RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2018-04-13T08:06:10.000Z",
                400, "NL04RABO0300065264", "withdrawal"));
        for (Transaction transaction : transactionList) {
            postTransaction(newSessionID, transaction);
        }

        for (int i = 0; i < transactionList.size(); i++) {
            String responseString = given().header("X-session-ID", newSessionID).
                    queryParam("limit", 1).queryParam("offset", i).
                    get("/api/v1/transactions").
                    then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
            ArrayList<Map<String, ?>> responseList = from(responseString).get("");
            assertThat(responseList.size(), equalTo(1));
            assertThat((String) responseList.get(0).get("date"), equalTo(transactionList.get(i).getDate()));
            assertThat((Float) responseList.get(0).get("amount"), equalTo(transactionList.get(i).getAmount()));
            assertThat((String) responseList.get(0).get("externalIBAN"), equalTo(transactionList.get(i).getExternalIBAN()));
            assertThat((String) responseList.get(0).get("type"), equalTo(transactionList.get(i).getType()));
        }
        String responseString = given().header("X-session-ID", newSessionID).
                get("/api/v1/transactions").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        ArrayList<Map<String, ?>> responseList = from(responseString).get("");
        assertThat(responseList.size(), equalTo(4));
        for (int i = 0; i < transactionList.size(); i++) {
            assertThat((String) responseList.get(i).get("date"), equalTo(transactionList.get(i).getDate()));
            assertThat((Float) responseList.get(i).get("amount"), equalTo(transactionList.get(i).getAmount()));
            assertThat((String) responseList.get(i).get("externalIBAN"), equalTo(transactionList.get(i).getExternalIBAN()));
            assertThat((String) responseList.get(i).get("type"), equalTo(transactionList.get(i).getType()));
        }
    }

    @Test
    public void testPostTransaction() {
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000Z",
                100, "NL39RABO0300065264", "deposit");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(transaction).
                post("/api/v1/transactions").then().statusCode(401);
        given().contentType("application/json").
                body(transaction).header("X-session-ID", "A1B2C3D4E5").
                post("/api/v1/transactions").then().statusCode(401);

        // Test valid transaction post response and status code
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                post("/api/v1/transactions").
                then().statusCode(201).
                body("$", hasKey("id")).
                body("date", equalTo("2018-04-13T08:06:10.000Z")).
                body("amount", equalTo((float) 100)).
                body("externalIBAN", equalTo("NL39RABO0300065264")).
                body("type", equalTo("deposit"));

        // Test invalid input status code
        transaction.setType("xxx");
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                post("/api/v1/transactions").then().statusCode(405);
    }

    @Test
    public void testGetTransaction() {
        // Test invalid session ID status code
        when().get("/api/v1/transactions/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get("/api/v1/transactions/1").then().statusCode(401);

        // Test invalid transaction ID status code
        given().header("X-session-ID", sessionID).get("/api/v1/transactions/8381237").then().statusCode(404);

        // Test valid transaction response and status code
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000Z",
                100, "NL39RABO0300065264", "deposit");
        long transactionID = postTransaction(transaction);
        given().header("X-session-ID", sessionID).
                get("/api/v1/transactions/" + transactionID).
                then().statusCode(200).
                body("date", equalTo("2018-04-13T08:06:10.000Z")).
                body("amount", equalTo((float) 100)).
                body("externalIBAN", equalTo("NL39RABO0300065264")).
                body("type", equalTo("deposit"));
    }

    @Test
    public void testPutTransaction() {
        Transaction transaction = new Transaction("2015-04-13T08:06:10.000Z",
                75, "NL01RABO0300065264", "withdrawal");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(transaction).
                put("/api/v1/transactions/1").then().statusCode(401);
        given().contentType("application/json").
                body(transaction).header("X-session-ID", "A1B2C3D4E5").
                put("/api/v1/transactions/1").then().statusCode(401);

        // Test invalid transaction ID status code
        given().contentType("application/json").body(transaction).header("X-session-ID", sessionID).
                put("/api/v1/transactions/8381237").then().statusCode(404);

        // Test valid transaction put response and status code
        long transactionID = postTransaction(transaction);
        transaction.setDate("2013-04-13T08:06:10.000Z");
        transaction.setAmount(225);
        transaction.setExternalIBAN("NL02RABO0300065264");
        transaction.setType("deposit");
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                put("/api/v1/transactions/" + transactionID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("date", equalTo("2013-04-13T08:06:10.000Z")).
                body("amount", equalTo((float) 225)).
                body("externalIBAN", equalTo("NL02RABO0300065264")).
                body("type", equalTo("deposit"));

        // Test invalid input status code
        transaction.setType("xxx");
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                put("/api/v1/transactions/" + transactionID).then().statusCode(405);
    }

    @Test
    public void testDeleteTransaction() {
        // Test invalid session ID status code
        when().delete("/api/v1/transactions/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").delete("/api/v1/transactions/1").then().statusCode(401);

        // Test invalid transaction ID status code
        given().header("X-session-ID", sessionID).delete("/api/v1/transactions/8381237").then().statusCode(404);

        // Test valid transaction status code
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000Z",
                100, "NL39RABO0300065264", "deposit");
        long transactionID = postTransaction(transaction);
        given().header("X-session-ID", sessionID).delete("/api/v1/transactions/" + transactionID).
                then().statusCode(204);
    }

    @Test
    public void testAssignCategoryToTransaction() {
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000Z",
                100, "NL39RABO0300065264", "deposit");
        Category category1 = new Category("Groceries");
        Category category2 = new Category("Rent");
        long transactionID = postTransaction(transaction);
        long categoryID1 = postCategory(category1);
        long categoryID2 = postCategory(category2);
        Map<String, Long> categoryIDMap = new HashMap<>();
        categoryIDMap.put("category_id", categoryID1);

        // Test invalid session ID status code
        given().contentType("application/json").body(categoryIDMap).
                patch("/api/v1/transactions/1/category").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").
                contentType("application/json").body(categoryIDMap).
                patch("/api/v1/transactions/1/category").then().statusCode(401);

        // Test valid assignment
        String responseString = given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch("/api/v1/transactions/" + transactionID + "/category").
                then().statusCode(200).
                contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("category");
        assertThat((String) responseMap.get("name"), equalTo(category1.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID1));
        categoryIDMap.put("category_id", categoryID2);
        responseString = given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch("/api/v1/transactions/" + transactionID + "/category").
                then().statusCode(200).
                contentType(ContentType.JSON).extract().response().asString();
        responseMap = from(responseString).get("category");
        assertThat((String) responseMap.get("name"), equalTo(category2.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID2));

        // Test invalid transaction ID and invalid category ID
        given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch("/api/v1/transactions/7183291/category").
                then().statusCode(404);
        categoryIDMap.put("category_id", new Long(7183291));
        given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch("/api/v1/transactions/" + transactionID + "/category").
                then().statusCode(404);
    }

    @Test
    public void testGetCategories() {
        // Test invalid session ID status code
        when().get("/api/v1/categories").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get("/api/v1/categories").then().statusCode(401);

        // Test responses and status codes
        String newSessionID = getNewSessionID();
        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Groceries"));
        categoryList.add(new Category("Rent"));
        categoryList.add(new Category("Entertainment"));
        categoryList.add(new Category("Salary"));
        for (Category category : categoryList) {
            postCategory(newSessionID, category);
        }

        for (int i = 0; i < categoryList.size(); i++) {
            String responseString = given().header("X-session-ID", newSessionID).
                    queryParam("limit", 1).queryParam("offset", i).
                    get("/api/v1/categories").
                    then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
            ArrayList<Map<String, ?>> responseList = from(responseString).get("");
            assertThat(responseList.size(), equalTo(1));
            assertThat((String) responseList.get(0).get("name"), equalTo(categoryList.get(i).getName()));
        }
        String responseString = given().header("X-session-ID", newSessionID).
                get("/api/v1/categories").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        ArrayList<Map<String, ?>> responseList = from(responseString).get("");
        assertThat(responseList.size(), equalTo(4));
        for (int i = 0; i < categoryList.size(); i++) {
            assertThat((String) responseList.get(i).get("name"), equalTo(categoryList.get(i).getName()));
        }
    }

    @Test
    public void testPostCategory() {
        Category category = new Category("Groceries");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(category).
                post("/api/v1/categories").then().statusCode(401);
        given().contentType("application/json").
                body(category).header("X-session-ID", "A1B2C3D4E5").
                post("/api/v1/categories").then().statusCode(401);

        // Test valid category post response and status code
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                post("/api/v1/categories").
                then().statusCode(201).
                body("$", hasKey("id")).
                body("name", equalTo("Groceries"));

        // Test invalid input status code
        category.setName(null);
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                post("/api/v1/categories").then().statusCode(405);
    }

    @Test
    public void testGetCategory() {
        // Test invalid session ID status code
        when().get("/api/v1/categories/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get("/api/v1/categories/1").then().statusCode(401);

        // Test invalid category ID status code
        given().header("X-session-ID", sessionID).get("/api/v1/categories/8381237").then().statusCode(404);

        // Test valid category response and status code
        Category category = new Category("Groceries");
        long categoryID = postCategory(category);
        given().header("X-session-ID", sessionID).
                get("/api/v1/categories/" + categoryID).
                then().statusCode(200).
                body("name", equalTo("Groceries"));
    }

    @Test
    public void testPutCategory() {
        Category category = new Category("Groceries");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(category).
                put("/api/v1/categories/1").then().statusCode(401);
        given().contentType("application/json").
                body(category).header("X-session-ID", "A1B2C3D4E5").
                put("/api/v1/categories/1").then().statusCode(401);

        // Test invalid catery ID status code
        given().contentType("application/json").body(category).header("X-session-ID", sessionID).
                put("/api/v1/categories/8381237").then().statusCode(404);

        // Test valid category put response and status code
        long categoryID = postCategory(category);
        category.setName("Rent");
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                put("/api/v1/categories/" + categoryID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("name", equalTo("Rent"));

        // Test invalid input status code
        category.setName(null);
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                put("/api/v1/categories/" + categoryID).then().statusCode(405);
    }

    @Test
    public void deleteCategory() {
        // Test invalid session ID status code
        when().delete("/api/v1/categories/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").delete("/api/v1/categories/1").then().statusCode(401);

        // Test invalid category ID status code
        given().header("X-session-ID", sessionID).delete("/api/v1/categories/8381237").then().statusCode(404);

        // Test valid category status code
        Category category = new Category("Groceries");
        long categoryID = postCategory(category);
        given().header("X-session-ID", sessionID).delete("/api/v1/categories/" + categoryID).
                then().statusCode(204);
    }





    // Helper methods (not actual tests) down here

    public static String getNewSessionID() {
        String responseString = when().post("/api/v1/sessions").
                then().contentType(ContentType.JSON).
                extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return (String) responseMap.get("id");
    }

    public static Long postTransaction(Transaction transaction) {
        return postTransaction(sessionID, transaction);
    }

    public static Long postTransaction(String sessionID, Transaction transaction) {
        String responseString = given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                post("/api/v1/transactions").
                then().contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return new Long((Integer) responseMap.get("id"));
    }

    public static Long postCategory(Category category) {
        return postCategory(sessionID, category);
    }

    public static Long postCategory(String sessionID, Category category) {
        String responseString = given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                post("/api/v1/categories").
                then().contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return new Long((Integer) responseMap.get("id"));
    }

}