package nl.utwente.ing.testing;

import io.restassured.http.ContentType;
import nl.utwente.ing.testing.bean.Category;
import nl.utwente.ing.testing.bean.CategoryRule;
import nl.utwente.ing.testing.bean.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static io.restassured.RestAssured.*;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static nl.utwente.ing.testing.HelperFunctions.*;

public class InitialSystemTest {

    private static String sessionID;


    @BeforeAll
    public static void setup() {
        sessionID = getNewSessionID();
    }

    @Test
    public void testSessionIDRetrieval() {
        // Test status code and that body contains id field
        when().post(URI_PREFIX + "/sessions").
                then().statusCode(201).
                body("$", hasKey("id"));
    }

    @Test
    public void testGetTransactions() {
        // Test invalid session ID status code
        when().get(URI_PREFIX + "/transactions").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get(URI_PREFIX + "/transactions").then().statusCode(401);

        // Test responses and status codes
        String newSessionID = getNewSessionID();
        ArrayList<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.000CEST",
                100, "test", "NL01RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2015-04-13T10:06:10.002CEST",
                200, "test", "NL02RABO0300065264", "withdrawal"));
        transactionList.add(new Transaction("2015-04-13T12:06:10.003CEST",
                300, "test", "NL03RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2015-04-13T01:07:10.004CEST",
                400, "test", "NL04RABO0300065264", "withdrawal"));
        for (Transaction transaction : transactionList) {
            postTransaction(newSessionID, transaction);
        }

        for (int i = 0; i < transactionList.size(); i++) {
            String responseString = given().header("X-session-ID", newSessionID).
                    queryParam("limit", 1).queryParam("offset", i).
                    get(URI_PREFIX + "/transactions").
                    then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
            ArrayList<Map<String, ?>> responseList = from(responseString).get("");
            assertThat(responseList.size(), equalTo(1));
            assertThat((String) responseList.get(0).get("date"), equalTo(transactionList.get(i).getDate()));
            assertThat((Float) responseList.get(0).get("amount"), equalTo(transactionList.get(i).getAmount()));
            assertThat((String) responseList.get(0).get("description"), equalTo(transactionList.get(i).getDescription()));
            assertThat((String) responseList.get(0).get("externalIBAN"), equalTo(transactionList.get(i).getExternalIBAN()));
            assertThat((String) responseList.get(0).get("type"), equalTo(transactionList.get(i).getType()));
        }
        String responseString = given().header("X-session-ID", newSessionID).
                get(URI_PREFIX + "/transactions").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        ArrayList<Map<String, ?>> responseList = from(responseString).get("");
        assertThat(responseList.size(), equalTo(4));
        for (int i = 0; i < transactionList.size(); i++) {
            assertThat((String) responseList.get(i).get("date"), equalTo(transactionList.get(i).getDate()));
            assertThat((Float) responseList.get(i).get("amount"), equalTo(transactionList.get(i).getAmount()));
            assertThat((String) responseList.get(0).get("description"), equalTo(transactionList.get(i).getDescription()));
            assertThat((String) responseList.get(i).get("externalIBAN"), equalTo(transactionList.get(i).getExternalIBAN()));
            assertThat((String) responseList.get(i).get("type"), equalTo(transactionList.get(i).getType()));
        }
    }

    @Test
    public void testPostTransaction() {
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(transaction).
                post(URI_PREFIX + "/transactions").then().statusCode(401);
        given().contentType("application/json").
                body(transaction).header("X-session-ID", "A1B2C3D4E5").
                post(URI_PREFIX + "/transactions").then().statusCode(401);

        // Test valid transaction post response and status code
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/transactions").
                then().statusCode(201).
                body("$", hasKey("id")).
                body("date", equalTo("2018-04-13T08:06:10.000CEST")).
                body("amount", equalTo((float) 100)).
                body("description", equalTo("test")).
                body("externalIBAN", equalTo("NL39RABO0300065264")).
                body("type", equalTo("deposit"));

        // Test invalid input status code
        transaction.setType("xxx");
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/transactions").then().statusCode(405);
    }

    @Test
    public void testGetTransaction() {
        // Test invalid session ID status code
        when().get(URI_PREFIX + "/transactions/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get(URI_PREFIX + "/transactions/1").then().statusCode(401);

        // Test invalid transaction ID status code
        given().header("X-session-ID", sessionID).get(URI_PREFIX + "/transactions/8381237").then().statusCode(404);

        // Test valid transaction response and status code
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");
        long transactionID = postTransaction(sessionID, transaction);
        given().header("X-session-ID", sessionID).
                get(URI_PREFIX + "/transactions/" + transactionID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("date", equalTo("2018-04-13T08:06:10.000CEST")).
                body("amount", equalTo((float) 100)).
                body("description", equalTo("test")).
                body("externalIBAN", equalTo("NL39RABO0300065264")).
                body("type", equalTo("deposit"));
    }

    @Test
    public void testPutTransaction() {
        Transaction transaction = new Transaction("2015-04-13T08:06:10.000CEST",
                75, "test", "NL01RABO0300065264", "withdrawal");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(transaction).
                put(URI_PREFIX + "/transactions/1").then().statusCode(401);
        given().contentType("application/json").
                body(transaction).header("X-session-ID", "A1B2C3D4E5").
                put(URI_PREFIX + "/transactions/1").then().statusCode(401);

        // Test invalid transaction ID status code
        given().contentType("application/json").body(transaction).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/transactions/8381237").then().statusCode(404);

        // Test valid transaction put response and status code
        long transactionID = postTransaction(sessionID, transaction);
        transaction.setDate("2013-04-13T08:06:10.000CEST");
        transaction.setAmount(225);
        transaction.setExternalIBAN("NL02RABO0300065264");
        transaction.setType("deposit");
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/transactions/" + transactionID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("date", equalTo("2013-04-13T08:06:10.000CEST")).
                body("amount", equalTo((float) 225)).
                body("description", equalTo("test")).
                body("externalIBAN", equalTo("NL02RABO0300065264")).
                body("type", equalTo("deposit"));

        // Test invalid input status code
        transaction.setType("xxx");
        given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/transactions/" + transactionID).then().statusCode(405);
    }

    @Test
    public void testDeleteTransaction() {
        // Test invalid session ID status code
        when().delete(URI_PREFIX + "/transactions/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").delete(URI_PREFIX + "/transactions/1").then().statusCode(401);

        // Test invalid transaction ID status code
        given().header("X-session-ID", sessionID).delete(URI_PREFIX + "/transactions/8381237").then().statusCode(404);

        // Test valid transaction status code
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");
        long transactionID = postTransaction(sessionID, transaction);
        given().header("X-session-ID", sessionID).delete(URI_PREFIX + "/transactions/" + transactionID).
                then().statusCode(204);
    }

    @Test
    public void testAssignCategoryToTransaction() {
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");
        Category category1 = new Category("Groceries");
        Category category2 = new Category("Rent");
        long transactionID = postTransaction(sessionID, transaction);
        long categoryID1 = postCategory(sessionID, category1);
        long categoryID2 = postCategory(sessionID, category2);
        Map<String, Long> categoryIDMap = new HashMap<>();
        categoryIDMap.put("category_id", categoryID1);

        // Test invalid session ID status code
        given().contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/1/category").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").
                contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/1/category").then().statusCode(401);

        // Test valid assignment
        String responseString = given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/" + transactionID + "/category").
                then().statusCode(200).
                contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("category");
        assertThat((String) responseMap.get("name"), equalTo(category1.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID1));
        categoryIDMap.put("category_id", categoryID2);
        responseString = given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/" + transactionID + "/category").
                then().statusCode(200).
                contentType(ContentType.JSON).extract().response().asString();
        responseMap = from(responseString).get("category");
        assertThat((String) responseMap.get("name"), equalTo(category2.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID2));

        // Test invalid transaction ID and invalid category ID
        given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/7183291/category").
                then().statusCode(404);
        categoryIDMap.put("category_id", new Long(7183291));
        given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/" + transactionID + "/category").
                then().statusCode(404);
    }

    @Test
    public void testGetCategories() {
        // Test invalid session ID status code
        when().get(URI_PREFIX + "/categories").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get(URI_PREFIX + "/categories").then().statusCode(401);

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
                    get(URI_PREFIX + "/categories").
                    then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
            ArrayList<Map<String, ?>> responseList = from(responseString).get("");
            assertThat(responseList.size(), equalTo(1));
            assertThat((String) responseList.get(0).get("name"), equalTo(categoryList.get(i).getName()));
        }
        String responseString = given().header("X-session-ID", newSessionID).
                get(URI_PREFIX + "/categories").
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
                post(URI_PREFIX + "/categories").then().statusCode(401);
        given().contentType("application/json").
                body(category).header("X-session-ID", "A1B2C3D4E5").
                post(URI_PREFIX + "/categories").then().statusCode(401);

        // Test valid category post response and status code
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/categories").
                then().statusCode(201).
                body("$", hasKey("id")).
                body("name", equalTo("Groceries"));

        // Test invalid input status code
        category.setName(null);
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/categories").then().statusCode(405);
    }

    @Test
    public void testGetCategory() {
        // Test invalid session ID status code
        when().get(URI_PREFIX + "/categories/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get(URI_PREFIX + "/categories/1").then().statusCode(401);

        // Test invalid category ID status code
        given().header("X-session-ID", sessionID).get(URI_PREFIX + "/categories/8381237").then().statusCode(404);

        // Test valid category response and status code
        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        given().header("X-session-ID", sessionID).
                get(URI_PREFIX + "/categories/" + categoryID).
                then().statusCode(200).
                body("name", equalTo("Groceries"));
    }

    @Test
    public void testPutCategory() {
        Category category = new Category("Groceries");

        // Test invalid session ID status code
        given().contentType("application/json").
                body(category).
                put(URI_PREFIX + "/categories/1").then().statusCode(401);
        given().contentType("application/json").
                body(category).header("X-session-ID", "A1B2C3D4E5").
                put(URI_PREFIX + "/categories/1").then().statusCode(401);

        // Test invalid catery ID status code
        given().contentType("application/json").body(category).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categories/8381237").then().statusCode(404);

        // Test valid category put response and status code
        long categoryID = postCategory(sessionID, category);
        category.setName("Rent");
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categories/" + categoryID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("name", equalTo("Rent"));

        // Test invalid input status code
        category.setName(null);
        given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categories/" + categoryID).then().statusCode(405);
    }

    @Test
    public void deleteCategory() {
        // Test invalid session ID status code
        when().delete(URI_PREFIX + "/categories/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").delete(URI_PREFIX + "/categories/1").then().statusCode(401);

        // Test invalid category ID status code
        given().header("X-session-ID", sessionID).delete(URI_PREFIX + "/categories/8381237").then().statusCode(404);

        // Test valid category status code
        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        given().header("X-session-ID", sessionID).delete(URI_PREFIX + "/categories/" + categoryID).
                then().statusCode(204);
    }

    @Test
    public void categoryAtGetTransactions() {
        // Used to test whether the category is correctly displayed in the getTransactions request
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");
        Category category = new Category("Groceries");
        String newSessionID = getNewSessionID();
        long transactionID = postTransaction(newSessionID, transaction);
        long categoryID = postCategory(newSessionID, category);
        assignCategoryToTransaction(newSessionID, transactionID, categoryID);
        String responseString = given().header("X-session-ID", newSessionID).get(URI_PREFIX + "/transactions").
                then().statusCode(200).
                contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = (Map<String, ?>) ((ArrayList<Map<String, ?>>) from(responseString).get("")).get(0);
        assertThat((String) responseMap.get("date"), equalTo(transaction.getDate()));
        assertThat((Float) responseMap.get("amount"), equalTo(transaction.getAmount()));
        assertThat((String) responseMap.get("description"), equalTo(transaction.getDescription()));
        assertThat((String) responseMap.get("externalIBAN"), equalTo(transaction.getExternalIBAN()));
        assertThat((String) responseMap.get("type"), equalTo(transaction.getType()));
        responseMap = (Map<String, ?>) responseMap.get("category");
        assertThat((String) responseMap.get("name"), equalTo(category.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID));
    }

    @Test
    public void testCategoryAtGetTransaction() {
        // Used to test whether the category is correctly displayed in the getTransaction request
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");
        Category category = new Category("Groceries");
        long transactionID = postTransaction(sessionID, transaction);
        long categoryID = postCategory(sessionID, category);
        assignCategoryToTransaction(sessionID, transactionID, categoryID);
        String responseString = given().header("X-session-ID", sessionID).
                get(URI_PREFIX + "/transactions/" + transactionID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("date", equalTo("2018-04-13T08:06:10.000CEST")).
                body("amount", equalTo((float) 100)).
                body("description", equalTo("test")).
                body("externalIBAN", equalTo("NL39RABO0300065264")).
                body("type", equalTo("deposit")).
                contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("category");
        assertThat((String) responseMap.get("name"), equalTo(category.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID));
    }

    @Test
    public void testCategoryAtPutTransaction() {
        // Used to test whether the category is correctly displayed in the putTransaction request
        Transaction transaction = new Transaction("2018-04-13T08:06:10.000CEST",
                100, "test", "NL39RABO0300065264", "deposit");
        Category category = new Category("Groceries");
        long transactionID = postTransaction(sessionID, transaction);
        long categoryID = postCategory(sessionID, category);
        assignCategoryToTransaction(sessionID, transactionID, categoryID);
        String responseString = given().header("X-session-ID", sessionID).
                contentType("application/json").body(transaction).
                put(URI_PREFIX + "/transactions/" + transactionID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("date", equalTo("2018-04-13T08:06:10.000CEST")).
                body("amount", equalTo((float) 100)).
                body("description", equalTo("test")).
                body("externalIBAN", equalTo("NL39RABO0300065264")).
                body("type", equalTo("deposit")).
                contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("category");
        assertThat((String) responseMap.get("name"), equalTo(category.getName()));
        assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID));
    }

    @Test
    public void testFilterOnCategories() {
        // Used to test whether filtering on categories works correctly in the getTransactions request
        Category category1 = new Category("Groceries");
        Category category2 = new Category("Rent");
        ArrayList<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.000CEST",
                100, "test", "NL01RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2016-04-13T08:06:10.001CEST",
                200, "test", "NL02RABO0300065264", "withdrawal"));
        transactionList.add(new Transaction("2017-04-13T08:06:10.002CEST",
                300, "test", "NL03RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2018-04-13T08:06:10.003CEST",
                400, "test", "NL04RABO0300065264", "withdrawal"));
        String newSessionID = getNewSessionID();
        long categoryID1 = postCategory(newSessionID, category1);
        long categoryID2 = postCategory(newSessionID, category2);
        for (Transaction transaction : transactionList) {
            long transactionID = postTransaction(newSessionID, transaction);
            if (transactionID % 2 == 1) {
                assignCategoryToTransaction(newSessionID, transactionID, categoryID1);
            } else {
                assignCategoryToTransaction(newSessionID, transactionID, categoryID2);
            }
        }

        String responseString = given().header("X-session-ID", newSessionID).
                queryParam("category", "Groceries").
                get(URI_PREFIX + "/transactions").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        ArrayList<Map<String, ?>> responseList = from(responseString).get("");
        assertThat(responseList.size(), equalTo(2));
        for (int i = 0; i < 2; i++) {
            long transactionID = new Long((Integer) responseList.get(i).get("id"));
            Map<String, ?> responseMap = (Map<String, ?>) responseList.get(i).get("category");
            assertThat((String) responseMap.get("name"), equalTo(category1.getName()));
            assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID1));
        }
        responseString = given().header("X-session-ID", newSessionID).
                queryParam("category", "Rent").
                get(URI_PREFIX + "/transactions").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        responseList = from(responseString).get("");
        assertThat(responseList.size(), equalTo(2));
        for (int i = 0; i < 2; i++) {
            long transactionID = new Long((Integer) responseList.get(i).get("id"));
            Map<String, ?> responseMap = (Map<String, ?>) responseList.get(i).get("category");
            assertThat((String) responseMap.get("name"), equalTo(category2.getName()));
            assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID2));
        }
    }

    @Test
    public void testGetCategoryRule() {
        // Test invalid session ID status code
        when().get(URI_PREFIX + "/categoryRules/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get(URI_PREFIX + "/categoryRules/1").then().statusCode(401);

        // Test invalid categoryRule ID status code
        given().header("X-session-ID", sessionID).get(URI_PREFIX + "/categoryRules/8381237").then().statusCode(404);

        // Test valid categoryRule response and status code
        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        CategoryRule categoryRule = new CategoryRule("test", "000", "", categoryID, false);
        long categoryRuleID = postCategoryRule(sessionID, categoryRule);
        given().header("X-session-ID", sessionID).
                get(URI_PREFIX + "/categoryRules/" + categoryRuleID).
                then().statusCode(200);
    }

    @Test
    public void testPostCategoryRule() {
        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        CategoryRule categoryRule = new CategoryRule("test", "000", "", categoryID, false);

        // Test valid category post response and status code
        given().contentType("application/json").
                body(categoryRule).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/categoryRules").
                then().statusCode(201).
                body("$", hasKey("id")).
                body("description", equalTo("test")).
                body("iBAN", equalTo("000")).
                body("type", equalTo("")).
                body("category_id", equalTo((int) categoryID)).
                body("applyOnHistory", equalTo(false));

        // Test invalid input status code
        categoryRule.setDescription(null);
        given().contentType("application/json").
                body(categoryRule).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categoryRules").then().statusCode(405);


    }

    @Test
    public void testGetCategoryRules() {
        // Test invalid session ID status code
        when().get(URI_PREFIX + "/categoryRules").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").get(URI_PREFIX + "/categoryRules").then().statusCode(401);

        // Test responses and status codes
        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        ArrayList<CategoryRule> categoryRuleList = new ArrayList<>();
        categoryRuleList.add(new CategoryRule("test1", "000", "", categoryID, false));
        categoryRuleList.add(new CategoryRule("test2", "ABN000", "", categoryID, true));
        categoryRuleList.add(new CategoryRule("test3", "123", "deposit", categoryID, true));
        categoryRuleList.add(new CategoryRule("test4", "ING", "withdrawal", categoryID, false));
        for (CategoryRule c : categoryRuleList) {
            postCategoryRule(sessionID, c);
        }

        String responseString = given().header("X-session-ID", sessionID).
                get(URI_PREFIX + "/categoryRules").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        ArrayList<Map<String, ?>> responseList = from(responseString).get("");
        assertThat(responseList.size(), equalTo(4));
        for (int i = 0; i < categoryRuleList.size(); i++) {
            assertThat((String) responseList.get(i).get("description"), equalTo(categoryRuleList.get(i).getDescription()));
            assertThat((String) responseList.get(i).get("iBAN"), equalTo(categoryRuleList.get(i).getiBAN()));
            assertThat((String) responseList.get(i).get("type"), equalTo(categoryRuleList.get(i).getType()));
            assertThat(new Long((Integer) responseList.get(i).get("category_id")), equalTo(categoryRuleList.get(i).getCategory_id()));
            assertThat((Boolean) responseList.get(i).get("applyOnHistory"), equalTo(categoryRuleList.get(i).getApplyOnHistory()));
        }
    }

    @Test
    public void testPutCategoryRule() {
        Category category1 = new Category("Groceries");
        long category1ID = postCategory(sessionID, category1);
        Category category2 = new Category("Utwente");
        long category2ID = postCategory(sessionID, category2);
        CategoryRule categoryRule = new CategoryRule("test", "123", "deposit", category1ID, false);
        long categoryRuleID = postCategoryRule(sessionID, categoryRule);

        // Test invalid session ID status code
        given().contentType("application/json").
                body(categoryRule).
                put(URI_PREFIX + "/categoryRules/1").then().statusCode(401);
        given().contentType("application/json").
                body(categoryRule).header("X-session-ID", "A1B2C3D4E5").
                put(URI_PREFIX + "/categoryRules/1").then().statusCode(401);

        // Test invalid categoryRule ID status code
        given().contentType("application/json").body(categoryRule).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categoryRules/8381237").then().statusCode(404);

        // Test valid categoryRule put response and status code
        categoryRule.setDescription("nottest");
        categoryRule.setiBAN("456");
        categoryRule.setType("");
        categoryRule.setCategory_id(category2ID);
        categoryRule.setApplyOnHistory(true);

        given().contentType("application/json").
                body(categoryRule).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categoryRules/" + categoryRuleID).
                then().statusCode(200).
                body("$", hasKey("id")).
                body("description", equalTo("nottest")).
                body("iBAN", equalTo("456")).
                body("type", equalTo("")).
                body("category_id", equalTo((int) category2ID)).
                body("applyOnHistory", equalTo(false));


        // Test invalid input status code
        categoryRule.setDescription(null);
        given().contentType("application/json").
                body(categoryRule).header("X-session-ID", sessionID).
                put(URI_PREFIX + "/categoryRules/" + categoryRuleID).then().statusCode(405);
    }

    @Test
    public void testDeleteCategoryRule() {
        // Test invalid session ID status code
        when().delete(URI_PREFIX + "/categoryRules/1").then().statusCode(401);
        given().header("X-session-ID", "A1B2C3D4E5").delete(URI_PREFIX + "/categoryRules/1").then().statusCode(401);

        // Test invalid categoryRule ID status code
        given().header("X-session-ID", sessionID).delete(URI_PREFIX + "/categoryRules/8381237").then().statusCode(404);

        // Test valid categoryRule status code
        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        CategoryRule categoryRule = new CategoryRule("test", "123", "deposit", categoryID, false);
        long categoryRuleID = postCategoryRule(sessionID, categoryRule);
        given().header("X-session-ID", sessionID).delete(URI_PREFIX + "/categoryRules/" + categoryRuleID).
                then().statusCode(204);
    }

    @Test
    public void testCategoryRuleOnTransactionsNoHistory() {

        String sessionID = getNewSessionID();

        Category category = new Category("Groceries");
        long categoryID = postCategory(sessionID, category);
        CategoryRule categoryRule = new CategoryRule("test", "000", "", categoryID, false);
        postCategoryRule(sessionID, categoryRule);

        // test whether the categoryRule correctly assigns categories to new transactions.
        ArrayList<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.001CEST",
                100, "test123", "NL01RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2016-04-13T08:06:10.002CEST",
                200, "test", "NL02RABO0300065264", "withdrawal"));
        for (Transaction transaction : transactionList) {
            long transactionID = postTransaction(sessionID, transaction);
            String responseString = given().header("X-session-ID", sessionID).
                    contentType("application/json").body(transaction).
                    put(URI_PREFIX + "/transactions/" + transactionID).
                    then().statusCode(200).
                    body("$", hasKey("id")).
                    body("date", equalTo(transaction.getDate())).
                    body("amount", equalTo(transaction.getAmount())).
                    body("description", equalTo(transaction.getDescription())).
                    body("externalIBAN", equalTo(transaction.getExternalIBAN())).
                    body("type", equalTo(transaction.getType())).
                    contentType(ContentType.JSON).extract().response().asString();
            Map<String, ?> responseMap = from(responseString).get("category");
            assertThat((String) responseMap.get("name"), equalTo(category.getName()));
            assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID));
        }

        // test if the categoryRule doesnt apply to not matching transactions
        transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.000CEST",
                100, "test", "NL01RABO0311165264", "deposit"));
        transactionList.add(new Transaction("2016-04-13T08:06:10.000CEST",
                200, "tell", "NL02RABO0300065264", "withdrawal"));
        for (Transaction transaction : transactionList) {
            long transactionID = postTransaction(sessionID, transaction);
            String responseString = given().header("X-session-ID", sessionID).
                    contentType("application/json").body(transaction).
                    put(URI_PREFIX + "/transactions/" + transactionID).
                    then().statusCode(200).
                    body("$", hasKey("id")).
                    body("date", equalTo(transaction.getDate())).
                    body("amount", equalTo(transaction.getAmount())).
                    body("description", equalTo(transaction.getDescription())).
                    body("externalIBAN", equalTo(transaction.getExternalIBAN())).
                    body("type", equalTo(transaction.getType())).
                    contentType(ContentType.JSON).extract().response().asString();
            Map<String, ?> responseMap = from(responseString).get("category");
            assertThat(responseMap, equalTo(null));
        }
    }

    @Test
    public void testCategoryRuleOnTransactionsWithHistory() {

        String sessionID = getNewSessionID();

        // test whether the categoryRule with applyOnHistory = true will apply to these transactions, posted before the rule
        HashMap<Transaction, Long> transactionMatchingIDHashMap = new HashMap<>();
        ArrayList<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.000CEST",
                100, "test", "NL01RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2016-04-13T08:06:10.000CEST",
                200, "test123", "NL02RABO0300065264", "withdrawal"));
        for (Transaction transaction : transactionList) {
            transactionMatchingIDHashMap.put(transaction, postTransaction(sessionID, transaction));
        }

        // test whether the categoryRule with applyOnHistory = true will not apply to these transactions, posted before the rule
        HashMap<Transaction, Long> transactionNonMatchingIDHashMap = new HashMap<>();
        transactionList = new ArrayList<>();
        transactionList.add(new Transaction("2015-04-13T08:06:10.001CEST",
                100, "twente", "NL01RABO0300065264", "deposit"));
        transactionList.add(new Transaction("2016-04-13T08:06:10.002CEST",
                200, "test123", "NL02RABO0311165264", "withdrawal"));
        for (Transaction transaction : transactionList) {
            transactionNonMatchingIDHashMap.put(transaction, postTransaction(sessionID, transaction));
        }


        Category category = new Category("Rent");
        long categoryID = postCategory(sessionID, category);
        category = new Category("Groceries");
        postCategory(sessionID, category);
        category.setName("Rent");

        CategoryRule categoryRule = new CategoryRule("test", "000", "", categoryID, true);
        postCategoryRule(sessionID, categoryRule);


        for (Transaction transaction : transactionMatchingIDHashMap.keySet()) {
            long transactionID = transactionMatchingIDHashMap.get(transaction);
            String responseString = given().header("X-session-ID", sessionID).
                    contentType("application/json").body(transaction).
                    put(URI_PREFIX + "/transactions/" + transactionID).
                    then().statusCode(200).
                    body("$", hasKey("id")).
                    body("date", equalTo(transaction.getDate())).
                    body("amount", equalTo(transaction.getAmount())).
                    body("description", equalTo(transaction.getDescription())).
                    body("externalIBAN", equalTo(transaction.getExternalIBAN())).
                    body("type", equalTo(transaction.getType())).
                    contentType(ContentType.JSON).extract().response().asString();
            Map<String, ?> responseMap = from(responseString).get("category");
            assertThat((String) responseMap.get("name"), equalTo(category.getName()));
            assertThat(new Long((Integer) responseMap.get("id")), equalTo(categoryID));
        }

        for (Transaction transaction : transactionNonMatchingIDHashMap.keySet()) {
            long transactionID = transactionNonMatchingIDHashMap.get(transaction);
            String responseString = given().header("X-session-ID", sessionID).
                    contentType("application/json").body(transaction).
                    put(URI_PREFIX + "/transactions/" + transactionID).
                    then().statusCode(200).
                    body("$", hasKey("id")).
                    body("date", equalTo(transaction.getDate())).
                    body("amount", equalTo(transaction.getAmount())).
                    body("description", equalTo(transaction.getDescription())).
                    body("externalIBAN", equalTo(transaction.getExternalIBAN())).
                    body("type", equalTo(transaction.getType())).
                    contentType(ContentType.JSON).extract().response().asString();
            Map<String, ?> responseMap = from(responseString).get("category");
            assertThat(responseMap, equalTo(null));
        }
    }

    @Test
    public void testBalanceHistoryWithoutTransactions() {
        String newSessionID = getNewSessionID();

        // test invalid sessionID
        given().header("X-session-ID", "A1B2C3D4E5").
                get(URI_PREFIX + "/balance/history").
                then().statusCode(401);

        // test invalid input given
        given().header("X-session-ID", newSessionID).queryParam("interval", 10).
                queryParam("intervals", 10).
                get(URI_PREFIX + "/balance/history").
                then().statusCode(405);
        given().header("X-session-ID", newSessionID).queryParam("interval", "minute").
                queryParam("intervals", 10).
                get(URI_PREFIX + "/balance/history").
                then().statusCode(405);


        // test default settings without transactions in the system
        String responseString = given().header("X-session-ID", newSessionID).
                get(URI_PREFIX + "/balance/history").
                then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
        ArrayList<Map<String, ?>> responseList = from(responseString).get("");
        long previousTimestamp = 0;
        for (int i = 0; i < responseList.size(); i++) {
            assertThat((Float) responseList.get(i).get("open"), equalTo(new Float(0)));
            assertThat((Float) responseList.get(i).get("close"), equalTo(new Float(0)));
            assertThat((Float) responseList.get(i).get("high"), equalTo(new Float(0)));
            assertThat((Float) responseList.get(i).get("low"), equalTo(new Float(0)));
            assertThat((Float) responseList.get(i).get("volume"), equalTo(new Float(0)));
            long timestamp = new Long((Integer) responseList.get(i).get("timeStamp"));
            if (previousTimestamp != 0) {
                long difference = previousTimestamp - timestamp;
                assertThat(difference > 2400000 && difference < 2700000, equalTo(true));
            }
            previousTimestamp = timestamp;
        }

        // test without transactions in the system
        String[] intervalTimes = {"hour", "day", "week", "month", "year"};
        for (int k = 0; k < intervalTimes.length; k++) {
            responseString = given().header("X-session-ID", newSessionID).queryParam("interval", intervalTimes[k]).
                    queryParam("intervals", 10).get(URI_PREFIX + "/balance/history").
                    then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
            responseList = from(responseString).get("");
            previousTimestamp = 0;
            for (int i = 0; i < responseList.size(); i++) {
                assertThat((Float) responseList.get(i).get("open"), equalTo(new Float(0)));
                assertThat((Float) responseList.get(i).get("close"), equalTo(new Float(0)));
                assertThat((Float) responseList.get(i).get("high"), equalTo(new Float(0)));
                assertThat((Float) responseList.get(i).get("low"), equalTo(new Float(0)));
                assertThat((Float) responseList.get(i).get("volume"), equalTo(new Float(0)));
                long timestamp = new Long((Integer) responseList.get(i).get("timeStamp"));
                if (intervalTimes[k].equals("month")) {
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference > 2400000 && difference < 2700000, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("year")) {
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference > 31500000 && difference < 31700000, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("week")) {
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference == 604800, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("day")) {
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference == 86400, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("hour")) {
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference == 3600, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                }
            }
        }
    }

    @Test
    public void testBalanceHistoryWithTransactions() {
        String newSessionID = getNewSessionID();
        ArrayList<Transaction> transactionList = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Calendar c = new GregorianCalendar();
        dateFormat.setCalendar(c);
        c.setTimeInMillis(Instant.now().getEpochSecond() * 1000);

        c.add(Calendar.HOUR_OF_DAY, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL01RABO0300065264", "deposit"));

        c.add(Calendar.HOUR_OF_DAY, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL02RABO0300065264", "deposit"));

        c.add(Calendar.DAY_OF_YEAR, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL03RABO0300065264", "deposit"));

        c.add(Calendar.DAY_OF_YEAR, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL04RABO0300065264", "deposit"));

        c.add(Calendar.WEEK_OF_YEAR, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL01RABO0300065264", "deposit"));

        c.add(Calendar.WEEK_OF_YEAR, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL02RABO0300065264", "deposit"));

        c.add(Calendar.MONTH, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL03RABO0300065264", "deposit"));

        c.add(Calendar.MONTH, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL04RABO0300065264", "withdrawal"));

        c.add(Calendar.YEAR, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL03RABO0300065264", "deposit"));

        c.add(Calendar.YEAR, -1);
        transactionList.add(new Transaction(dateFormat.format(c.getTime()),
                100, "test", "NL04RABO0300065264", "withdrawal"));

        for (Transaction transaction : transactionList) {
            postTransaction(newSessionID, transaction);
        }


        float[] hourOpen = {600, 500, 400};
        float[] hourClose = {600, 600, 500};
        float[] hourHigh = {600, 600, 500};
        float[] hourLow = {600, 500, 400};
        float[] hourVolume = {0, 100, 100};

        float[] dayOpen = {400, 300, 200};
        float[] dayClose = {600, 400, 300};
        float[] dayHigh = {600, 400, 300};
        float[] dayLow = {400, 300, 200};
        float[] dayVolume = {200, 100, 100 };

        float[] weekOpen = {200, 100, 0};
        float[] weekClose = {600, 200, 100};
        float[] weekHigh = {600, 200, 100};
        float[] weekLow = {200, 100, 0};
        float[] weekVolume = {400, 100, 100};

        float[] monthOpen = {0, -100, 0};
        float[] monthClose = {600, 0, -100};
        float[] monthHigh = {600, 0, 0};
        float[] monthLow = {0, -100, -100};
        float[] monthVolume = {600, 100, 100};

        float[] yearOpen = {0, -100, 0};
        float[] yearClose = {600, 0, -100};
        float[] yearHigh = {600, 0, 0};
        float[] yearLow = {-100, -100, -100};
        float[] yearVolume = {800, 100, 100};
        String[] intervalTimes = {"hour", "day", "week", "month", "year"};

        for (int k = 0; k < intervalTimes.length; k++) {
            String responseString = given().header("X-session-ID", newSessionID).queryParam("interval", intervalTimes[k]).
                    queryParam("intervals", 3).get(URI_PREFIX + "/balance/history").
                    then().statusCode(200).contentType(ContentType.JSON).extract().response().asString();
            ArrayList<Map<String, ?>> responseList = from(responseString).get("");
            long previousTimestamp = 0;
            for (int i = 0; i < responseList.size(); i++) {
                long timestamp = new Long((Integer) responseList.get(i).get("timeStamp"));
                if (intervalTimes[k].equals("month")) {
                    assertThat((Float) responseList.get(i).get("open"), equalTo(monthOpen[i]));
                    assertThat((Float) responseList.get(i).get("close"), equalTo(monthClose[i]));
                    assertThat((Float) responseList.get(i).get("high"), equalTo(monthHigh[i]));
                    assertThat((Float) responseList.get(i).get("low"), equalTo(monthLow[i]));
                    assertThat((Float) responseList.get(i).get("volume"), equalTo(monthVolume[i]));
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference > 2400000 && difference < 2700000, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("year")) {
                    assertThat((Float) responseList.get(i).get("open"), equalTo(yearOpen[i]));
                    assertThat((Float) responseList.get(i).get("close"), equalTo(yearClose[i]));
                    assertThat((Float) responseList.get(i).get("high"), equalTo(yearHigh[i]));
                    assertThat((Float) responseList.get(i).get("low"), equalTo(yearLow[i]));
                    assertThat((Float) responseList.get(i).get("volume"), equalTo(yearVolume[i]));
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference > 31500000 && difference < 31700000, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("week")) {
                    assertThat((Float) responseList.get(i).get("open"), equalTo(weekOpen[i]));
                    assertThat((Float) responseList.get(i).get("close"), equalTo(weekClose[i]));
                    assertThat((Float) responseList.get(i).get("high"), equalTo(weekHigh[i]));
                    assertThat((Float) responseList.get(i).get("low"), equalTo(weekLow[i]));
                    assertThat((Float) responseList.get(i).get("volume"), equalTo(weekVolume[i]));
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference == 604800, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("day")) {
                    assertThat((Float) responseList.get(i).get("open"), equalTo(dayOpen[i]));
                    assertThat((Float) responseList.get(i).get("close"), equalTo(dayClose[i]));
                    assertThat((Float) responseList.get(i).get("high"), equalTo(dayHigh[i]));
                    assertThat((Float) responseList.get(i).get("low"), equalTo(dayLow[i]));
                    assertThat((Float) responseList.get(i).get("volume"), equalTo(dayVolume[i]));
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference == 86400, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                } else if (intervalTimes[k].equals("hour")) {
                    assertThat((Float) responseList.get(i).get("open"), equalTo(hourOpen[i]));
                    assertThat((Float) responseList.get(i).get("close"), equalTo(hourClose[i]));
                    assertThat((Float) responseList.get(i).get("high"), equalTo(hourHigh[i]));
                    assertThat((Float) responseList.get(i).get("low"), equalTo(hourLow[i]));
                    assertThat((Float) responseList.get(i).get("volume"), equalTo(hourVolume[i]));
                    if (previousTimestamp != 0) {
                        long difference = previousTimestamp - timestamp;
                        assertThat(difference == 3600, equalTo(true));
                    }
                    previousTimestamp = timestamp;
                }
            }
        }
    }

}