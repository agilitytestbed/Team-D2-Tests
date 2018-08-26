package nl.utwente.ing.testing;

import io.restassured.http.ContentType;
import nl.utwente.ing.testing.bean.Category;
import nl.utwente.ing.testing.bean.CategoryRule;
import nl.utwente.ing.testing.bean.SavingGoal;
import nl.utwente.ing.testing.bean.Transaction;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.path.json.JsonPath.from;

public class HelperFunctions {

    public static final String URI_PREFIX = "/api/v1";


    public static String getNewSessionID() {
        String responseString = when().post(URI_PREFIX + "/sessions").
                then().contentType(ContentType.JSON).
                extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return (String) responseMap.get("id");
    }

    public static Long postTransaction(String sessionID, Transaction transaction) {
        String responseString = given().contentType("application/json").
                body(transaction).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/transactions").
                then().contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return new Long((Integer) responseMap.get("id"));
    }

    public static Long postCategory(String sessionID, Category category) {
        String responseString = given().contentType("application/json").
                body(category).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/categories").
                then().contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return new Long((Integer) responseMap.get("id"));
    }

    public static void assignCategoryToTransaction(String sessionID, long transactionID, long categoryID) {
        Map<String, Long> categoryIDMap = new HashMap<>();
        categoryIDMap.put("category_id", categoryID);
        given().header("X-session-ID", sessionID).
                contentType("application/json").body(categoryIDMap).
                patch(URI_PREFIX + "/transactions/" + transactionID + "/category");
    }

    public static Long postCategoryRule(String sessionID, CategoryRule categoryRule) {
        String responseString = given().contentType("application/json").
                body(categoryRule).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/categoryRules").
                then().contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return new Long((Integer) responseMap.get("id"));
    }

    public static Long postSavingGoal(String sessionID, SavingGoal savingGoal) {
        String responseString = given().contentType("application/json").
                body(savingGoal).header("X-session-ID", sessionID).
                post(URI_PREFIX + "/savingGoals").
                then().contentType(ContentType.JSON).extract().response().asString();
        Map<String, ?> responseMap = from(responseString).get("");
        return new Long((Integer) responseMap.get("id"));
    }
}
