package com.softdesign.devintensive.data.network.req;

/**
 * Модель запроса: аутентификация пользователя
 */
public class UserLoginReq {

    private String email;
    private String password;

    public UserLoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
