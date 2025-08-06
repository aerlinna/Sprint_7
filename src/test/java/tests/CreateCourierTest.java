package tests;

import com.github.javafaker.Faker;
import config.Courier;
import config.QAScooterServiceClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.Matchers.*;

@Epic("API тесты сервиса доставки")
@Feature("Создание курьера")
public class CreateCourierTest extends QAScooterServiceClient {

    private static final Faker faker = new Faker(new Locale("en"));
    private Integer courierId;

    private Courier generateCourier() {
        return new Courier(
                "cr_" + faker.name().username() + faker.number().digits(3),
                faker.internet().password(6, 12),
                faker.name().firstName()
        );
    }

    @Test
    @Story("Успешное создание курьера")
    @Description("Проверяем, что курьера можно создать, и ответ содержит ok: true")
    public void createCourierSuccess() {
        Courier courier = generateCourier();

        Response response = createCourierResponse(courier);
        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = loginCourier(courier.getLogin(), courier.getPassword())
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    @Story("Создание двух одинаковых курьеров")
    @Description("Нельзя создать двух курьеров с одинаковым логином")
    public void createDuplicateCourierFails() {
        Courier courier = generateCourier();


        createCourierResponse(courier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));


        createCourierResponse(courier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

        courierId = loginCourier(courier.getLogin(), courier.getPassword())
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    @Story("Создание курьера без пароля")
    @Description("Если отсутствует пароль, запрос возвращает 400 и сообщение об ошибке")
    public void createCourierWithoutPasswordFails() {
        Courier courier = new Courier(
                "cr_" + faker.name().username() + faker.number().digits(3),
                null,
                faker.name().firstName()
        );

        createCourierResponse(courier)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @After
    @Step("Удаляем созданного курьера после теста")
    public void tearDown() {
        if (courierId != null) {
            try {
                deleteCourier(courierId)
                        .then()
                        .statusCode(anyOf(is(200), is(404)));
            } catch (Exception e) {
                System.err.println("Не удалось удалить курьера с id = " + courierId + ": " + e.getMessage());
            }
        }
    }
}
