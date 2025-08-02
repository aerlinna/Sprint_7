package tests;

import config.QAScooterServiceClient;
import config.TestData;
import config.Courier;
import config.CourierLogin;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

@Epic("API тесты сервиса доставки")
@Feature("Логин курьера")
public class LoginCourierTest extends QAScooterServiceClient {

    private Integer courierId;

    @Before
    @Step("Создание тестового курьера")
    public void setUp() {
        Courier courier = new Courier(
                TestData.COURIER_LOGIN,
                TestData.COURIER_PASSWORD,
                TestData.COURIER_FIRST_NAME
        );
        createCourierResponse(courier).then().statusCode(201);
    }

    @Test
    @Story("Успешный логин курьера")
    @Description("Курьер может авторизоваться с корректными учетными данными")
    public void loginSuccess() {
        CourierLogin login = new CourierLogin(TestData.COURIER_LOGIN, TestData.COURIER_PASSWORD);
        Response res = loginCourier(login);

        res.then()
                .statusCode(200)
                .body("id", notNullValue());

        courierId = res.jsonPath().getInt("id");
    }

    @Test
    @Story("Ошибка при неправильном логине или пароле")
    @Description("Система возвращает ошибку, если неверно указан логин или пароль")
    public void loginFailWrongCredentials() {
        CourierLogin login = new CourierLogin(TestData.COURIER_LOGIN + "wrong", TestData.COURIER_PASSWORD);
        loginCourier(login)
                .then()
                .statusCode(404)
                .body("message", not(emptyString()));
    }

    @Test
    @Story("Ошибка при отсутствии обязательных полей")
    @Description("Запрос возвращает ошибку, если отсутствует обязательное поле login или password")
    public void loginFailMissingFields() {
        // Отсутствует login
        loginCourier(new CourierLogin(null, TestData.COURIER_PASSWORD))
                .then()
                .statusCode(400)
                .body("message", not(emptyString()));

        // Отсутствует password
        loginCourier(new CourierLogin(TestData.COURIER_LOGIN, null))
                .then()
                .statusCode(400)
                .body("message", not(emptyString()));
    }

    @Test
    @Story("Ошибка при попытке авторизации несуществующего курьера")
    @Description("Система возвращает ошибку, если пользователь не существует")
    public void loginFailNonexistentUser() {
        CourierLogin login = new CourierLogin("nonexistent" + System.currentTimeMillis(), "somepass");
        loginCourier(login)
                .then()
                .statusCode(404)
                .body("message", not(emptyString()));
    }

    @After
    @Step("Удаление тестового курьера")
    public void cleanUp() {
        if (courierId != null) {
            try {
                deleteCourier(courierId)
                        .then()
                        .statusCode(200);
            } catch (Exception e) {
                System.out.println("Не удалось удалить курьера с id: " + courierId);
            }
        }
    }
}
