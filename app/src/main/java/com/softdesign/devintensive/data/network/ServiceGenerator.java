package com.softdesign.devintensive.data.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.softdesign.devintensive.data.network.interceptors.HeaderInterceptor;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.DevintensiveApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Генератор клиента (сервис) для взаимодействия с REST API
 */
public class ServiceGenerator {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder sBuilder =
            new Retrofit.Builder()
                    .baseUrl(AppConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()); // подключение GSON-конвертора

    public static <S> S createService(Class<S> serviceClass) {

        // подключение логирования
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // задает уровень логирования

        // внедряем интерсепторы в http-клиент
        httpClient.addInterceptor(new HeaderInterceptor()); // добавление заголовков (токен, id-пользователя, User Agent)
        httpClient.addInterceptor(logging); // логирование

        httpClient.connectTimeout(AppConfig.MAX_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        httpClient.readTimeout(AppConfig.MAX_READ_TIMEOUT, TimeUnit.MILLISECONDS);

        // подключаем кеш к http-клиенту
        httpClient.cache(new Cache(DevintensiveApplication.getContext().getCacheDir(), Integer.MAX_VALUE));

        // интерсептор для использования интсрументов отладки Stetho
        httpClient.addNetworkInterceptor(new StethoInterceptor());

        Retrofit retrofit = sBuilder
                .client(httpClient.build())
                .build();
        return retrofit.create(serviceClass);
    }
}
