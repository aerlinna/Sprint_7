package tests;

import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class DeleteCourierTest {
    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/";
    private static final String CREATE_COURIER = "/api/v1/courier";
    private static final String LOGIN_COURIER = "/api/v1/courier/login";
    private static final String DELETE_COURIER = "/api/v1/courier/{id}";

    private Courier courier;
    private Integer courierId;

    @Before
    public void setUp() {
        courier = new Courier("pygmy_owl", "wingardium", "Hermes");

        // Создаём курьера
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(courier)
                .post(CREATE_COURIER)
                .then().statusCode(201);

        // Получаем id через логин
        ValidatableResponse loginResponse = given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"login\":\"" + courier.getLogin() + "\", \"password\":\"" + courier.getPassword() + "\"}")
                .post(LOGIN_COURIER)
                .then().statusCode(200).body("id", notNullValue());

        courierId = loginResponse.extract().path("id");
    }

    @Test
    public void deleteCourier_successfully() {
        // Удаляем курьера
        given().baseUri(BASE_URL)
                .pathParam("id", courierId)
                .delete(DELETE_COURIER)
                .then().statusCode(200)
                .body("ok", equalTo(true));
    }

    @Test
    public void deleteCourier_withoutId_returnsError() {
        // DELETE без id в path
        given().baseUri(BASE_URL)
                .delete("/api/v1/courier/")
                .then().statusCode(404)
                .body("message", equalTo("Not Found."));
    }

    @Test
    public void deleteCourier_withNonExistentId_returnsError() {
        // Несуществующий ID
        given().baseUri(BASE_URL)
                .pathParam("id", 999999)
                .delete(DELETE_COURIER)
                .then().statusCode(404)
                .body("message", equalTo("Курьера с таким id нет."));
    }

    @After
    public void cleanUp() {
        // На случай, если основной тест не удалил курьера
        if (courierId != null) {
            given().baseUri(BASE_URL)
                    .pathParam("id", courierId)
                    .delete(DELETE_COURIER);
        }
    }
}
