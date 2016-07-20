package com.softdesign.devintensive.data.managers;

import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevintensiveApplication;

import java.util.ArrayList;
import java.util.List;

public class PreferencesManager {

    private SharedPreferences mSharedPreferences;

    private static final String[] USER_FIELDS = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_MAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GIT_KEY,
            ConstantManager.USER_BIO_KEY,
    };

    private static final String[] USER_VALUES = {
            ConstantManager.USER_RATING_VALUE,
            ConstantManager.USER_CODE_LINES_VALUE,
            ConstantManager.USER_PROJECTS_VALUE
    };

    private static final String[] USER_NAMES = {
            ConstantManager.USER_FIRST_NAME,
            ConstantManager.USER_SECOND_NAME,
    };

    public PreferencesManager() {
        this.mSharedPreferences = DevintensiveApplication.getSharedPreferences();
    }

    /**
     * Сохраняет данные профайла пользователя в Shared Preferences
     * @param userFields список, содержащий данные профайла пользователя
     */
    public void saveUserProfileData(List<String> userFields) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_FIELDS.length; i++) {
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }

        editor.apply();
    }

    /**
     * Считывает данные профайла пользователя из Shared Preferences
     * @return список, содержащий данные профайла пользователя
     */
    public List<String> loadUserProfileData() {
        List<String> userFields = new ArrayList<>();
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_PHONE_KEY, "null"));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_MAIL_KEY, "null"));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_VK_KEY, "null"));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_GIT_KEY, "null"));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_BIO_KEY, "null"));
        return userFields;
    }

    /**
     * Сохраняет URI фотографии пользователя в Shared Preferences
     * @param uri URI фотографии
     */
    public void saveUserPhoto(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PHOTO_KEY, uri.toString());
        editor.apply();
    }

    /**
     * Считывает URI фотографии пользователя из Shared Preferences
     * @return URI фотографии
     */
    public Uri loadUserPhoto() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PHOTO_KEY,
                "android.resource://com.softdesign.devintensive/drawable/user_bg"));
    }

    /**
     * Сохраняет URL фотографии пользователя в Shared Preferences
     * @param photoUrl URL фотографии пользователя
     */
    public void saveUserPhotoUrl(String photoUrl) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PHOTO_URL_KEY, photoUrl);
        editor.apply();
    }

    /**
     * Считывает URL фотографии пользователя из Shared Preferences
     * @return URL фотографии пользователя
     */
    public String loadUserPhotoUrl() {
        return mSharedPreferences.getString(ConstantManager.USER_PHOTO_URL_KEY, null);
    }

    /**
     * Сохраняет URL аватара пользователя в Shared Preferences
     * @param avatarUrl URL аватара пользователя
     */
    public void saveUserAvatarUrl(String avatarUrl) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_AVATAR_URL_KEY, avatarUrl);
        editor.apply();
    }

    /**
     * Считывает URL аватара пользователя из Shared Preferences
     * @return URL аватара пользователя
     */
    public String loadUserAvatarUrl() {
        return mSharedPreferences.getString(ConstantManager.USER_AVATAR_URL_KEY, null);
    }

    /**
     * Сохраняет данные пользователя (рейтинг, строки кода, проекты) в Shared Preferences
     * @param userValues массив, содержащий данные
     */
    public void saveUserProfileValues(int[] userValues) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_VALUES.length; i++) {
            editor.putString(USER_VALUES[i], String.valueOf(userValues[i]));
        }

        editor.apply();
    }

    /**
     * Считывает данные пользователя (рейтинг, строки кода, проекты) из Shared Preferences
     * @return списочный массив, содержащий данные
     */
    public List<String> loadUserProfileValues() {
        List<String> userValues = new ArrayList<>();
        userValues.add(mSharedPreferences.getString(ConstantManager.USER_RATING_VALUE, "0"));
        userValues.add(mSharedPreferences.getString(ConstantManager.USER_CODE_LINES_VALUE, "0"));
        userValues.add(mSharedPreferences.getString(ConstantManager.USER_PROJECTS_VALUE, "0"));
        return userValues;
    }

    /**
     * Сохраняет имя и фамилию пользователя в Shared Preferences
     * @param userNames массив, содержащий имя и фамилию
     */
    public void saveUserFullName(String[] userNames) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_NAMES.length; i++) {
            editor.putString(USER_NAMES[i], userNames[i]);
        }

        editor.apply();
    }

    /**
     * Считывает имя и фамилию пользователя из Shared Preferences
     * @return списочный массив, содержащий имя и фамилию
     */
    public List<String> loadUserFullName() {
        List<String> userNames = new ArrayList<>();
        userNames.add(mSharedPreferences.getString(ConstantManager.USER_FIRST_NAME, " "));
        userNames.add(mSharedPreferences.getString(ConstantManager.USER_SECOND_NAME, " "));
        return userNames;
    }

    /**
     * Сохраняет логин (E-mail) пользователя в Shared Preferences
     * @param userLogin токен
     */
    public void saveUserLogin(String userLogin) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_LOGIN_KEY, userLogin);
        editor.apply();
    }

    /**
     * Считывает логин (E-mail) пользователя из Shared Preferences
     * @return логин (E-mail) пользователя
     */
    public String getUserLogin() {
        return mSharedPreferences.getString(ConstantManager.USER_LOGIN_KEY, "null");
    }


    /**
     * Сохраняет токен авторизации в Shared Preferences
     * @param authToken токен
     */
    public void saveAuthToken(String authToken) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.AUTH_TOKEN_KEY, authToken);
        editor.apply();
    }

    /**
     * Считывает токен авторизации из Shared Preferences
     * @return токен авторизации
     */
    public String getAuthToken() {
        return mSharedPreferences.getString(ConstantManager.AUTH_TOKEN_KEY, "null");
    }

    /**
     * Сохраняет идентификатор пользователя в Shared Preferences
     * @param userId токен
     */
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_ID_KEY, userId);
        editor.apply();
    }

    /**
     * Считывает идентификатор пользователя из Shared Preferences
     * @return идентификатор пользователя
     */
    public String getUserId() {
        return mSharedPreferences.getString(ConstantManager.USER_ID_KEY, "null");
    }
}
