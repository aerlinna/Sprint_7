package tests;

import com.github.javafaker.Faker;
import config.Courier;
import config.CourierLogin;
import config.OrderPOJO;
import io.qameta.allure.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("API тесты сервиса доставки")
@Feature("Принятие заказа")
@RunWith(Parameterized.class)
public class AcceptOrderTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String CREATE_COURIER = "/api/v1/courier";
    private static final String LOGIN_COURIER = "/api/v1/courier/login";
    private static final String CREATE_ORDER = "/api/v1/orders";
    private static final String ACCEPT_ORDER = "/api/v1/orders/accept/{id}";
    private static final String DELETE_COURIER = "/api/v1/courier/{id}";

    private Integer courierId;
    private Integer orderTrack;

    private static final Faker faker = new Faker(new Locale("en"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Parameterized.Parameter
    public String[] colors;

    @Parameterized.Parameters(name = "Цвета заказа: {0}")
    public static Object[][] colorsData() {
        return new Object[][] {
                {new String[]{"BLACK"}},
                {new String[]{"GREY"}},
                {new String[]{"BLACK", "GREY"}},
                {new String[]{}}
        };
    }

    @Before
    public void setUp() {
        courierId = createCourierAndLogin();
        orderTrack = createOrder(colors);
    }

    @Test
    @Story("Успешное принятие заказа")
    @Description("Возвращает ok: true")
    public void acceptOrderSuccess() {
        given()
                .baseUri(BASE_URL)
                .pathParam("id", orderTrack)
                .queryParam("courierId", courierId)
                .when()
                .put(ACCEPT_ORDER)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @After
    public void tearDown() {
        if (courierId != null) {
            try {
                given()
                        .baseUri(BASE_URL)
                        .pathParam("id", courierId)
                        .when()
                        .delete(DELETE_COURIER)
                        .then()
                        .statusCode(anyOf(is(200), is(404))); // 404 если уже удален
            } catch (Exception e) {
                System.err.println("Ошибка при удалении курьера: " + e.getMessage());
            }
        }
    }

    private Integer createCourierAndLogin() {
        String login = "acc_" + faker.name().username() + faker.number().digits(3);
        String password = faker.internet().password(6, 10);
        String firstName = faker.name().firstName();

        given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(new Courier(login, password, firstName))
                .when()
                .post(CREATE_COURIER)
                .then()
                .statusCode(201);

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(new CourierLogin(login, password))
                .when()
                .post(LOGIN_COURIER)
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    private Integer createOrder(String[] colors) {
        List<String> colorList = (colors == null || colors.length == 0) ? Collections.emptyList() : Arrays.asList(colors);

        OrderPOJO order = new OrderPOJO(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.address().streetAddress(),
                faker.address().cityName(),
                faker.phoneNumber().phoneNumber(),
                faker.number().numberBetween(1, 10),
                LocalDate.now().plusDays(faker.number().numberBetween(1, 30)).format(DATE_FORMATTER),
                faker.lorem().sentence(),
                colorList
        );

        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(order)
                .when()
                .post(CREATE_ORDER)
                .then()
                .statusCode(201)
                .body("track", notNullValue())
                .extract()
                .path("track");
    }
}
