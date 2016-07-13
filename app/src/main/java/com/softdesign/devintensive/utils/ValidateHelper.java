package com.softdesign.devintensive.utils;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс ValidateHelper инкапсулирует методы для валидации и форматирования данных
 * в полях профайла пользователя.
 */
public class ValidateHelper {

    /**
     * Осуществляет валидацию номера телефона (наличие ведущего символа +, проверку длины номера:
     * от 11 до 20 цифр) и нормализацию номера (для использования в intent-ах).
     * @param phoneStr исходная строка, содержащая номер телефона.
     * @return нормализованную строку, содержащую номер телефона, если номер прошел валидацию
     * и null - в противном случае.
     */
    public static String getValidatedPhone(String phoneStr) {
        String normalizedPhone = phoneStr.trim();
        if (normalizedPhone.charAt(0) != '+') {
            return null;
        }
        normalizedPhone = normalizedPhone.replaceAll("[^\\d]", "");
        if (normalizedPhone.length() >= 11 && normalizedPhone.length() <= 20) {
            return "+" + normalizedPhone;
        }
        return null;
    }

    /**
     * Осуществляет форматирование обычного номера мобильного телефона (содержащего 11 цифр)
     * согласно маске +х ххх ххх-хх-хх.
     * @param phoneStrNorm нормализованная методом {@link ValidateHelper#getValidatedPhone} строка,
     *                     содержащая номер телефона.
     * @return отформатированную строку с номером мобильного телефона, если номер содержит 11 цифр.
     * В противном случае строка с номером возвращается без изменений.
     */
    public static String formatRegularPhone(String phoneStrNorm) {
        if (phoneStrNorm.length() == 12) {
            StringBuilder phoneBuilder = new StringBuilder();

            phoneBuilder.append(phoneStrNorm.substring(0, 2));
            phoneBuilder.append(" (");
            phoneBuilder.append(phoneStrNorm.substring(2, 5));
            phoneBuilder.append(") ");
            phoneBuilder.append(phoneStrNorm.substring(5, 8));
            phoneBuilder.append("-");
            phoneBuilder.append(phoneStrNorm.substring(8, 10));
            phoneBuilder.append("-");
            phoneBuilder.append(phoneStrNorm.substring(10, 12));

            return phoneBuilder.toString();
        }
        return phoneStrNorm;
    }

    /**
     * Осуществляет валидацию E-mail адреса
     * @param emailStr строка, содержащая адрес E-mail
     * @return нормализованную строку (удаляются начальные и конечные пробелы) с адресом E-mail,
     * если адрес прошел валидацию или null - в противном случае.
     */
    @Nullable
    public static String getValidatedEmail(String emailStr) {
        String regExp = "^[\\w\\-\\.]{3,}@[\\w\\-\\.]{2,}\\.[\\w]{2,}$";
        //String regExp = "^.{3,}@.{2,}\\..{2,}$"; // или так (или как-нибудь еще...)
        Pattern pattern = Pattern.compile(regExp);
        String normalizedEmail = emailStr.trim();
        Matcher matcher = pattern.matcher(normalizedEmail);
        if (matcher.matches()) {
            return normalizedEmail;
        }
        return null;
    }

    /**
     * Осуществляет валидацию и нормализацию URL-адреса аккаунта VK к виду vk.com/ххх.
     * @param vkUrlStr строка, содержащая URL-адрес
     * @return нормализованную строку, содержащию адрес аккаунта VK, если адрес прошел валидацию
     * или null - в противном случае.
     */
    @Nullable
    public static String getValidatedVkUrl(String vkUrlStr) {
        return getTruncatedUrl(vkUrlStr, "vk.com");
    }

    /**
     * Осуществляет валидацию и нормализацию URL-адреса аккаунта GitHub к виду github.com/ххх.
     * @param gitUrlStr строка, содержащая URL-адрес
     * @return нормализованную строку, содержащию адрес аккаунта VK, если адрес прошел валидацию
     * или null - в противном случае.
     */
    @Nullable
    public static String getValidatedGitUrl(String gitUrlStr) {
        return getTruncatedUrl(gitUrlStr, "github.com");
    }

    /**
     * Осуществляет валидацию и нормализацию URL-адреса виду domain/ххх.
     * @param url строка, содержащая URL-адрес
     * @param domain строка, содержащая домен
     * @return нормализованную строку, содержащию адрес, если адрес прошел валидацию
     * или null - в противном случае.
     */
    @Nullable
    private static String getTruncatedUrl(String url, String domain) {
        String normalizedUrl = url.trim();
        String protocol = "https://";
        if (normalizedUrl.startsWith(protocol)) {
            normalizedUrl = normalizedUrl.substring(protocol.length());
        }
        if (normalizedUrl.startsWith(domain + "/")) {
            Pattern pattern = Pattern.compile("[\\w\\-\\./]+");
            Matcher matcher = pattern.matcher(normalizedUrl.substring(domain.length() + 1));
            if (matcher.matches()) {
                return normalizedUrl;
            }
        }
        return null;
    }

}
