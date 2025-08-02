package tests;

import config.QAScooterServiceClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

@Epic("API тесты сервиса доставки")
@Feature("Работа с заказами")
public class GetOrdersListTest extends QAScooterServiceClient {

    private int createdOrderTrack;

    @Test
    @Story("Получение списка заказов")
    @Description("Проверяю, что список заказов возвращается и содержит хотя бы один заказ")
    public void getAllOrdersSuccess() {
        createdOrderTrack = createOrder(new String[]{"BLACK"});

        Response response = getAllOrders();
        response.then()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("orders", instanceOf(java.util.List.class))
                .body("orders.size()", greaterThan(0));
    }

    @After
    @Step("Отменяем тестовый заказ")
    public void tearDown() {
        if (createdOrderTrack > 0) {
            cancelOrder(createdOrderTrack)
                    .then()
                    .statusCode(anyOf(is(200), is(404)));
        }
    }
}
