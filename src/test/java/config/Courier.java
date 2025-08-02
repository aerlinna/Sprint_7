// класс для создания курьера
package config;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Courier {
    private final String login;
    private final String password;
    private final String firstName;
}
