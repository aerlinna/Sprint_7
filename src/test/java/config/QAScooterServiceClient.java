package config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.BeforeClass;

import java.util.Arrays;
import java.util.List;

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

    protected Response loginCourier(String login, String password) {
        CourierLoginPOJO body = new CourierLoginPOJO(login, password);

        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(body))
                .when()
                .post(COURIER_LOGIN);
    }

    protected Response createCourier(String login, String password, String name) {
        CourierCreatePOJO body = new CourierCreatePOJO(login, password, name);

        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(body))
                .when()
                .post(COURIER_CREATE);
    }

    protected Response createCourierNoLogin(String password, String name) {
        CourierCreatePOJO body = new CourierCreatePOJO("", password, name);

        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(body))
                .when()
                .post(COURIER_CREATE);
    }

    protected Response createCourierNoPassword(String login, String name) {
        CourierCreatePOJO body = new CourierCreatePOJO(login, "", name);

        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(body))
                .when()
                .post(COURIER_CREATE);
    }

    protected Response deleteCourier(String login, String password) {
        Response loginResponse = loginCourier(login, password);
        String id = loginResponse.jsonPath().getString("id");

        return given()
                .header("Content-type", "application/json")
                .pathParam("id", id)
                .when()
                .delete(COURIER_DELETE + "{id}");
    }

    protected int createOrder(String[] colors) {
        List<String> colorList = colors != null ? Arrays.asList(colors) : null;

        OrderPOJO order = new OrderPOJO(
                "Harry",
                "Potter",
                "Hogwarts Castle, Scotland",
                "Hogsmeade Station",
                "+441234567890",
                7,
                "2025-10-31",
                "Send via owl post, leave at the Fat Lady’s portrait",
                colorList
        );

        Response response = given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(CREATING_AN_ORDER);

        return response.jsonPath().getInt("track");
    }

    protected Response getOrderByTrack(int track) {
        return given()
                .header("Content-type", "application/json")
                .queryParam("t", track)
                .when()
                .get(RECEIVE_AN_ORDER);
    }
}
