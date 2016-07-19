package com.softdesign.devintensive.utils;

/**
 * Конфигурация приложения
 */
public interface AppConfig {
    String BASE_URL = "http://devintensive.softdesign-apps.ru/api/";
    String FORGOT_PASS_URL = "http://devintensive.softdesign-apps.ru/forgotpass";
    int MAX_CONNECT_TIMEOUT = 5000;
    int MAX_READ_TIMEOUT = 5000;
    int START_DELAY = 1500;
    int SPLASH_DELAY = 500;
}
