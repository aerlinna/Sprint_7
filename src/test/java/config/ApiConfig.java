package config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeTrue;

public final class ApiConfig {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";

    private ApiConfig() {}

    public static void init() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured());
    }

    public static boolean checkApiAvailable() {
        try {
            given()
                    .baseUri("https://qa-scooter.praktikum-services.ru")
                    .when().get("/")
                    .then().statusCode(anyOf(is(200), is(404), is(401)));
        } catch (Exception e) {
            assumeTrue("API недоступен: " + e.getMessage(), false);
        }
        return true;
    }
}