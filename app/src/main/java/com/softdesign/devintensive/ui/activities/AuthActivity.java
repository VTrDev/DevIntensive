package com.softdesign.devintensive.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements  View.OnClickListener {

    private Button mSignIn;
    private TextView mRememberPassword;
    private EditText mLogin, mPassword;
    private CoordinatorLayout mCoordinatorLayout;

    private DataManager mDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mDataManager = DataManager.getInstance();

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);
        mSignIn = (Button) findViewById(R.id.login_btn);
        mRememberPassword = (TextView) findViewById(R.id.remember_txt);
        mLogin = (EditText) findViewById(R.id.login_email_et);
        mPassword = (EditText) findViewById(R.id.login_password_et);

        mRememberPassword.setOnClickListener(this);
        mSignIn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogin.setText("trusov.public@gmail.com");
        mPassword.setText("sda1092014");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                signIn();
                break;
            case R.id.remember_txt:
                rememberPassword();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Открывает в браузере страницу восстановления пароля.
     */
    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(AppConfig.FORGOT_PASS_URL));
        startActivity(rememberIntent);
    }

    /**
     * Обрабатывает успешную аутентификацию пользователя в приложении.
     * Сохраняет данные в Shared Preferences и БД
     * @param userModel модель данных пользователя, полученных с сервера.
     */
    private void loginSuccess(final UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveUserId(userModel.getData().getUser().getId());
        saveUserValues(userModel);
        saveUserData(userModel);
        saveUserFullName(userModel);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(AuthActivity.this, SplashActivity.class);
                loginIntent.putExtra(ConstantManager.LOGIN_COMPLETE_KEY, true);
                startActivity(loginIntent);
            }
        }, AppConfig.START_DELAY);


    }

    /**
     * Выполняет аутентификацию пользователя в приложении.
     */
    private void signIn() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {

            final String login = mLogin.getText().toString();

            Call<UserModelRes> call = mDataManager.loginUser(
                    new UserLoginReq(login, mPassword.getText().toString()));
            showProgress();
            // асинхронный вызов
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        loginSuccess(response.body());
                        mDataManager.getPreferencesManager().saveUserLogin(login);
                        hideProgress();
                    } else if (response.code() == 404) {
                        hideProgress();
                        showSnackbar(getString(R.string.auth_invalid_credentials));
                    } else {
                        hideProgress();
                        showSnackbar(getString(R.string.msg_smth_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    hideProgress();
                    showSnackbar(getString(R.string.msg_network_error));
                    Log.e(TAG, "Network error: " + t.getMessage());
                }
            });

        } else {
            showSnackbar(getString(R.string.msg_network_isnt_available));
        }
    }

    /**
     * Извлекает статистические данные пользователя (рейтинг, строки кода, проекты)
     * из модели {@link UserModelRes} и сохраняет их в Shared Preferences
     * @param userModel модель данных пользователя
     */
    private void saveUserValues(UserModelRes userModel) {
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };

        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }

    /**
     * Извлекает данные профиля пользователя из модели {@link UserModelRes}
     * и сохраняет их в Shared Preferences
     * @param userModel модель данных пользователя
     */
    private void saveUserData(UserModelRes userModel) {
        List<String> userFields = new ArrayList<>();
        userFields.add(userModel.getData().getUser().getContacts().getPhone());
        userFields.add(userModel.getData().getUser().getContacts().getEmail());
        userFields.add(userModel.getData().getUser().getContacts().getVk());
        userFields.add(userModel.getData().getUser().getRepositories().getRepo().get(0).getGit());
        String bio = userModel.getData().getUser().getPublicInfo().getBio();
        userFields.add(bio.isEmpty() ? getString(R.string.user_profile_empty_bio) : bio);

        mDataManager.getPreferencesManager().saveUserProfileData(userFields);
    }

    /**
     * Извлекает имя и фамилию пользователя из модели {@link UserModelRes}
     * и сохраняет их в Shared Preferences
     * @param userModel модель данных пользователя
     */
    private void saveUserFullName(UserModelRes userModel) {
        String[] userNames = {
                userModel.getData().getUser().getFirstName(),
                userModel.getData().getUser().getSecondName()
        };

        mDataManager.getPreferencesManager().saveUserFullName(userNames);
    }




}
