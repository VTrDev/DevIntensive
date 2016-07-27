package com.softdesign.devintensive.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.ConstantManager;

public class BaseActivity extends AppCompatActivity {
    public static final String TAG = ConstantManager.TAG_PREFIX + "BaseActivity";
    protected ProgressDialog mProgressDialog;

    private DataManager mDataManager = DataManager.getInstance();

    /**
     * Отображает диалог прогресса выполнения длительных операций
     */
    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.custom_dialog);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.progress_splash);
        } else {
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.progress_splash);
        }
    }

    /**
     * Скрывает диалог прогресса выполнения длительных операций
     */
    public void hideProgress() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    /**
     * Отображает всплывающее сообщение об ошибке
     * @param message текст сообщения
     * @param error исключение, вызванное ошибкой
     */
    public void showError(String message, Exception error) {
        showToast(message);
        Log.e(TAG, String.valueOf(error));
    }

    /**
     * Отображает всплывающее текстовое сообщение
     * @param message текст сообщения
     */
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Реализует выход их приложения.
     * Сбрасывает токен. Перенаправляет на активность авторизации.
     */
    public void logout() {
        mDataManager.getPreferencesManager().saveAuthToken("");
        Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Проверяет, не является ли токен авторизации пустым.
     * @return true - если токен пустой, false - в противном случае.
     */
    public boolean checkTokenEmpty() {
        String authToken = mDataManager.getPreferencesManager().getAuthToken();
        return authToken.isEmpty();
    }

}
