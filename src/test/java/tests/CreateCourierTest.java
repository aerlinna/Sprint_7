package tests;

import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class CreateCourierTest {
    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_COURIER_URL = "/api/v1/courier";
    private static final String LOGIN_COURIER_URL = "/api/v1/courier/login";
    private static final String DELETE_COURIER_URL = "/api/v1/courier/{id}";

    private final String login = "owl_post_new_" + UUID.randomUUID().toString().substring(0, 5);
    private final String password = "alohomora";
    private final Courier courier = new Courier(login, password, "Hedwig");
    private Integer courierId = null;

    @Test
    public void createCourier_success() {
        ValidatableResponse createResponse = given().log().all()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(courier)
                .post(CREATE_COURIER_URL)
                .then().log().all();

        createResponse.assertThat().statusCode(201).body("ok", equalTo(true));


        String loginBody = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", login, password);

        ValidatableResponse loginResponse = given().log().all()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(loginBody)
                .post(LOGIN_COURIER_URL)
                .then().log().all();

        loginResponse.assertThat().statusCode(200);
        courierId = loginResponse.extract().path("id");
    }

    @After
    public void after() {
        if (courierId != null) {
            try {
                given().log().all()
                        .baseUri(BASE_URL)
                        .pathParam("id", courierId)
                        .delete(DELETE_COURIER_URL)
                        .then().log().all()
                        .statusCode(anyOf(is(200), is(204)));
            } catch (Exception e) {
                System.out.println("Не удалось удалить курьера: " + courierId);
                e.printStackTrace();
            }
        }
    }
}
