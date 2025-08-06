package tests;

import config.QAScooterServiceClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.*;

@Epic("API тесты сервиса доставки")
@Feature("Работа с заказами")
@RunWith(Parameterized.class)
public class CreateOrderTest extends QAScooterServiceClient {

    private int createdOrderTrack;

    @Parameterized.Parameter(0)
    public String[] colors;

    @Parameterized.Parameters(name = "Тестовые цвета: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}},
                {new String[]{"GREY"}},
                {new String[]{"BLACK", "GREY"}},
                {null}
        });
    }

    @Test
    @Story("Создание заказа с разными цветами")
    @Description("Проверяем, что заказ успешно создается с разными цветами и возвращается трек")
    public void createOrderWithColors() {
        createdOrderTrack = createOrder(colors);
        Assert.assertTrue("Трек должен быть положительным", createdOrderTrack > 0);

        Response response = getOrderByTrack(createdOrderTrack);
        response.then()
                .statusCode(200)
                .body("order.track", is(createdOrderTrack));
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
