// класс для логина курьера
package config;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class CourierLogin {
    private final String login;
    private final String password;
}
