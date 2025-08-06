package tests;

import com.github.javafaker.Faker;
import config.Courier;
import config.CourierLogin;
import io.qameta.allure.*;
import io.qameta.allure.Step;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Epic("API тесты сервиса доставки")
@Feature("Удаление курьера")
public class DeleteCourierTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_COURIER = "/api/v1/courier";
    private static final String LOGIN_COURIER = "/api/v1/courier/login";
    private static final String DELETE_COURIER = "/api/v1/courier/{id}";

    private Integer createdCourierId;
    private static final Faker faker = new Faker(new Locale("en"));

    private String courierLogin;
    private String courierPassword;

    @Before
    public void setUp() {
        courierLogin = "del_" + faker.name().username() + faker.number().digits(3);
        courierPassword = faker.internet().password(6, 12);
        String courierFirstName = faker.name().firstName();

        createCourier(courierLogin, courierPassword, courierFirstName);
        createdCourierId = loginCourier(courierLogin, courierPassword);
    }

    @Step("Создаем курьера с логином {login}")
    private void createCourier(String login, String password, String firstName) {
        given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(new Courier(login, password, firstName))
                .post(CREATE_COURIER)
                .then()
                .statusCode(201);
    }

    @Step("Логинимся под курьером {login}")
    private Integer loginCourier(String login, String password) {
        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(new CourierLogin(login, password))
                .post(LOGIN_COURIER)
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    @Story("Успешное удаление курьера")
    @Description("Возвращает ok: true при успешном удалении")
    @Step("Удаляем курьера с ID {courierId}")
    public void deleteCourierSuccess() {
        given()
                .baseUri(BASE_URL)
                .pathParam("id", createdCourierId)
                .delete(DELETE_COURIER)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
        createdCourierId = null;
    }

    @After
    public void tearDown() {
        if (createdCourierId != null) {
            try {
                given()
                        .baseUri(BASE_URL)
                        .pathParam("id", createdCourierId)
                        .delete(DELETE_COURIER)
                        .then()
                        .statusCode(200);
            } catch (Exception e) {
                System.err.println("Не удалось удалить курьера с ID " + createdCourierId + ": " + e.getMessage());
            }
        }
    }
}
