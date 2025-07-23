package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

@Epic("API тесты сервиса доставки")
@Feature("Приём заказа курьером")
public class AcceptOrderTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_COURIER = "/api/v1/courier";
    private static final String LOGIN_COURIER = "/api/v1/courier/login";
    private static final String CREATE_ORDER = "/api/v1/orders";
    private static final String ACCEPT_ORDER = "/api/v1/orders/accept";
    private static final String CANCEL_ORDER = "/api/v1/orders/cancel";
    private static final String DELETE_COURIER = "/api/v1/courier/{id}";

    private Integer courierId;
    private Integer orderTrack;
    private String courierLogin;
    private final String courierPassword = "accio123";

    @Before
    @Step("Подготовка данных: создание курьера и заказа")
    public void setUp() {
        courierLogin = "accept_owl_" + UUID.randomUUID().toString().substring(0, 5);
        Courier courier = new Courier(courierLogin, courierPassword, "Hedwig");

        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(courier)
                .post(CREATE_COURIER)
                .then().statusCode(201);

        courierId = given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"login\":\"" + courierLogin + "\", \"password\":\"" + courierPassword + "\"}")
                .post(LOGIN_COURIER)
                .then().statusCode(200)
                .extract()
                .path("id");

        orderTrack = given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Test\", \"lastName\":\"User\", \"address\":\"Street 1\", \"metroStation\":1, \"phone\":\"+79999999999\", \"rentTime\":5, \"deliveryDate\":\"2025-07-30\", \"comment\":\"Test order\"}")
                .post(CREATE_ORDER)
                .then().statusCode(201)
                .extract()
                .path("track");
    }

    @Test
    @Story("Успешный приём заказа")
    @Description("Курьер успешно принимает заказ")
    @Step("Курьер принимает заказ с courierId={courierId} и orderTrack={orderTrack}")
    public void acceptOrderSuccess() {
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"courierId\":" + courierId + ",\"orderId\":" + orderTrack + "}")
                .post(ACCEPT_ORDER)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Test
    @Story("Приём заказа без courierId")
    @Description("Ошибка при отсутствии courierId в теле запроса")
    @Step("Попытка принять заказ без courierId с orderTrack={orderTrack}")
    public void acceptOrderWithoutCourierId() {
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"orderId\":" + orderTrack + "}")
                .post(ACCEPT_ORDER)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для поиска"));
    }

    @Test
    @Story("Приём заказа с несуществующим courierId")
    @Description("Ошибка при передаче несуществующего courierId")
    @Step("Попытка принять заказ с неверным courierId=999999 и orderTrack={orderTrack}")
    public void acceptOrderInvalidCourierId() {
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"courierId\":999999,\"orderId\":" + orderTrack + "}")
                .post(ACCEPT_ORDER)
                .then()
                .statusCode(404)
                .body("message", anyOf(equalTo("Курьера с таким id нет"), equalTo("Not Found.")));
    }

    @Test
    @Story("Приём заказа без orderId")
    @Description("Ошибка при отсутствии orderId в теле запроса")
    @Step("Попытка принять заказ без orderId с courierId={courierId}")
    public void acceptOrderWithoutOrderId() {
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"courierId\":" + courierId + "}")
                .post(ACCEPT_ORDER)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для поиска"));
    }

    @Test
    @Story("Приём заказа с несуществующим orderId")
    @Description("Ошибка при передаче несуществующего orderId")
    @Step("Попытка принять заказ с courierId={courierId} и неверным orderId=999999")
    public void acceptOrderInvalidOrderId() {
        given().baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body("{\"courierId\":" + courierId + ",\"orderId\":999999}")
                .post(ACCEPT_ORDER)
                .then()
                .statusCode(404)
                .body("message", anyOf(equalTo("Заказа с таким id нет"), equalTo("Not Found.")));
    }

    @After
    @Step("Очистка данных: отмена заказа и удаление курьера")
    public void tearDown() {
        if (orderTrack != null) {
            try {
                Response cancelResponse = given().baseUri(BASE_URL)
                        .header("Content-Type", "application/json")
                        .body("{\"track\":" + orderTrack + "}")
                        .post(CANCEL_ORDER)
                        .andReturn();

                if (cancelResponse.statusCode() != 200) {
                    System.err.println("Не удалось отменить заказ с треком " + orderTrack);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при отмене заказа: " + e.getMessage());
            }
        }

        if (courierId != null) {
            try {
                given().baseUri(BASE_URL)
                        .pathParam("id", courierId)
                        .delete(DELETE_COURIER)
                        .then()
                        .statusCode(anyOf(is(200), is(204)));
            } catch (Exception e) {
                System.err.println("Ошибка при удалении курьера с id " + courierId + ": " + e.getMessage());
            }
        }
    }
}
