package config;
import java.util.List;
import java.util.Arrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.BeforeClass;

import static config.Endpoints.*;
import static io.restassured.RestAssured.given;
import static org.junit.Assume.assumeTrue;

public class QAScooterServiceClient {

    private static final Gson gson = new GsonBuilder().create();

    @BeforeClass
    public static void checkApiAvailability() {
        ApiConfig.init();
        assumeTrue("API недоступен", ApiConfig.checkApiAvailable());
    }

    public Response loginCourier(String login, String password) {
        return loginCourier(new CourierLogin(login, password));
    }

    public Response loginCourier(CourierLogin login) {
        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(login))
                .when()
                .post(COURIER_LOGIN);
    }

    public Response createCourierResponse(Courier courier) {
        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(courier))
                .when()
                .post(COURIER_CREATE);
    }

    public Response deleteCourier(Integer id) {
        return given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .when()
                .delete(COURIER_DELETE + "{id}");
    }

    // создаю заказ
    public int createOrder(String[] colors) {
        List<String> colorList = (colors != null && colors.length > 0) ? Arrays.asList(colors) : null;

        OrderPOJO order = new OrderPOJO(
                TestData.ORDER_FIRST_NAME,
                TestData.ORDER_LAST_NAME,
                TestData.ORDER_ADDRESS,
                TestData.ORDER_METRO_STATION,
                TestData.ORDER_PHONE,
                TestData.ORDER_RENT_TIME,
                TestData.ORDER_DELIVERY_DATE,
                TestData.ORDER_COMMENT,
                colorList
        );

        Response response = given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(order))
                .when()
                .post(CREATING_AN_ORDER);

        response.then().statusCode(201);
        return response.jsonPath().getInt("track");
    }

    // получаю список всех заказов
    public Response getAllOrders() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(GET_ORDERS);
    }

    // получаю заказ по номеру трека
    public Response getOrderByTrack(int track) {
        return given()
                .contentType(ContentType.JSON)
                .queryParam("t", track)
                .when()
                .get(RECEIVE_AN_ORDER);
    }

    // Отменить заказ по треку
    public Response cancelOrder(int track) {
        return given()
                .contentType(ContentType.JSON)
                .queryParam("track", track)
                .when()
                .put(ORDERS_CANCEL);
    }
}
