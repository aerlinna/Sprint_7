package config;

import lombok.Data;

@Data
public class CourierCreatePOJO {
    private String login;
    private String password;
    private String firstName;

    public CourierCreatePOJO(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }
}
