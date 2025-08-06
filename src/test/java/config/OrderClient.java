package config;

import config.OrderPOJO;
import config.ApiConfig;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderClient {

    public OrderClient() {
        ApiConfig.init();
    }

    public Response createOrder(OrderPOJO order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/v1/orders");
    }

    public Response cancelOrder(int track) {
        return given()
                .header("Content-type", "application/json")
                .delete("/api/v1/orders/cancel/" + track);
    }
}
