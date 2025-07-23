package tests;

import io.qameta.allure.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("API тесты сервиса доставки")
@Feature("Получение заказа по номеру")
public class GetOrderByNumberTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_ORDER = "/api/v1/orders";
    private static final String GET_ORDER_BY_TRACK = "/api/v1/orders/track";
    private static final String CANCEL_ORDER = "/api/v1/orders/cancel";

    private Integer orderTrack;

    @Before
    @Step("Создание заказа для теста")
    public void setUp() {
        orderTrack = given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Test\", \"lastName\":\"User\", \"address\":\"Some Street, 1\", " +
                        "\"metroStation\":1, \"phone\":\"+79999999999\", \"rentTime\":5, " +
                        "\"deliveryDate\":\"2025-07-30\", \"comment\":\"Test order\"}")
                .post(CREATE_ORDER)
                .then()
                .statusCode(201)
                .extract()
                .path("track");
    }

    @Test
    @Story("Успешное получение заказа по номеру")
    @Description("Получение заказа по номеру (track). Проверяем статус и содержание тела.")
    @Step("Получение заказа с номером {orderTrack}")
    public void getOrderByNumberSuccess() {
        given()
                .baseUri(BASE_URL)
                .queryParam("t", orderTrack)
                .get(GET_ORDER_BY_TRACK)
                .then()
                .statusCode(200)
                .body("order.track", equalTo(orderTrack))
                .body("order", notNullValue());
    }

    @Test
    @Story("Получение заказа с несуществующим номером")
    @Description("Запрос заказа с несуществующим номером должен вернуть 404 и сообщение об ошибке")
    @Step("Получение заказа с несуществующим номером 99999999")
    public void getOrderByNumberNotFound() {
        given()
                .baseUri(BASE_URL)
                .queryParam("t", 99999999)
                .get(GET_ORDER_BY_TRACK)
                .then()
                .statusCode(404)
                .body("message", equalTo("Заказ не найден"));
    }

    @After
    @Step("Отмена созданного заказа")
    public void tearDown() {
        if (orderTrack != null) {
            given()
                    .baseUri(BASE_URL)
                    .header("Content-Type", "application/json")
                    .body("{\"track\":" + orderTrack + "}")
                    .post(CANCEL_ORDER)
                    .then()
                    .statusCode(anyOf(is(200), is(404)));
        }
    }
}
