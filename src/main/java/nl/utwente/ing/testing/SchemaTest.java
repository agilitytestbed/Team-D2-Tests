package nl.utwente.ing.testing;

import nl.utwente.ing.testing.bean.Category;
import nl.utwente.ing.testing.bean.CategoryRule;
import nl.utwente.ing.testing.bean.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static nl.utwente.ing.testing.InitialSystemTest.*;


public class SchemaTest {

    public static String sessionID;
    public static Long transaction_1_ID;
    public static Long transaction_2_ID;
    public static Long category_1_ID;
    public static Long getCategory_2_ID;
    public static Long getCategoryRule_1_ID;


    @BeforeClass
    public static void getSessionID() {
        Category category_1 = new Category(" Groceries");
        Category category_2 = new Category("Clothes");
        Transaction transaction_1 = new Transaction("2015-04-13T08:06:10.000Z",
                100, "test1", "NL01RABO0300065264", "deposit");
        Transaction transaction_2 = new Transaction("2016-04-13T08:06:10.000Z",
                200, "test2", "NL02RABO0300065264", "withdrawal");
        CategoryRule categoryRule_1 = new CategoryRule("rule1", "NL01RABO0300065264",
                "deposit", 0, false);

        sessionID = getNewSessionID();
        transaction_1_ID = postTransaction(sessionID, transaction_1);
        transaction_2_ID = postTransaction(sessionID, transaction_2);
        category_1_ID = postCategory(sessionID, category_1);
        getCategory_2_ID = postCategory(sessionID, category_2);
        assignCategoryToTransaction(sessionID, transaction_1_ID, category_1_ID);
        assignCategoryToTransaction(sessionID, transaction_2_ID, getCategory_2_ID);
        getCategoryRule_1_ID = postCategoryRule(categoryRule_1);

    }

    @Test
    public void test_GET_transactions_JSON_schema() {

        when().
                get("/api/v1/transactions?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/TransactionList.json"));

    }

    @Test
    public void test_GET_transaction_ID_JSON_schema() {

        when().
                get("/api/v1/transactions/" + transaction_1_ID.toString() + "?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/Transaction.json"));

    }

    @Test
    public void test_GET_categories_JSON_schema() {

        when().
                get("/api/v1/categories?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/CategoryList.json"));

    }

    @Test
    public void test_GET_category_ID_JSON_schema() {

        when().
                get("/api/v1/categories/" + category_1_ID.toString() + "?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/Category.json"));

    }

    @Test
    public void test_GET_categoryRule_ID_JSON_schema() {
        when().
                get("/api/v1/categoryRules/" + getCategoryRule_1_ID.toString() + "?session_id=" + sessionID).
                then().
                assertThat().body(matchesJsonSchemaInClasspath("nl/utwente/ing/testing/schemas/CategoryRule.json"));
    }

}
