package tests;

import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoginCourierTest {
    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/";
    private static final String CREATE_COURIER_URL = "/api/v1/courier";
    private static final String LOGIN_COURIER_URL = "/api/v1/courier/login";
    private static final String DELETE_COURIER_URL = "/api/v1/courier/{id}";

    private final Courier courier = new Courier("owl_login", "lumos", "Errol");
    private Integer courierId = null;

    @Before
    public void setUp() {
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(courier)
                .post(CREATE_COURIER_URL)
                .then().statusCode(201);
    }

    @Test
    public void courierCanLogin_withValidCredentials() {
        ValidatableResponse loginResponse =
                given().baseUri(BASE_URL)
                        .header("Content-Type", "application/json")
                        .body("{\"login\": \"" + courier.getLogin() + "\", \"password\": \"" + courier.getPassword() + "\"}")
                        .post(LOGIN_COURIER_URL)
                        .then().log().all();

        loginResponse.assertThat().statusCode(200).body("id", notNullValue());

        courierId = loginResponse.extract().path("id");
    }

    @After
    public void cleanUp() {
        if (courierId != null) {
            try {
                given().baseUri(BASE_URL)
                        .pathParam("id", courierId)
                        .delete(DELETE_COURIER_URL)
                        .then().statusCode(200);
            } catch (Exception e) {
                System.out.println(" Не удалось удалить курьера: " + courierId);
            }
        }
    }
}
