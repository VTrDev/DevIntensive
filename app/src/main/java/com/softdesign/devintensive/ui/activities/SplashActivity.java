package com.softdesign.devintensive.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseActivity implements LoaderManager.LoaderCallbacks {

    private static final int DB_DATA_INS_LOADER = 0;

    private DataManager mDataManager;

    public static Bus bus;

    private static List<UserListRes.UserData> mUserDataList;

    /**
     * Loader для асинхронного сохранения данных пользователей
     * и информации об их репозиториях в БД
     */
    private static class DbUserDataInsLoader extends AsyncTaskLoader {

        public DbUserDataInsLoader(Context context) {
            super(context);
        }

        @Override
        public Object loadInBackground() {

            List<Repository> allRepositories = new ArrayList<>();
            List<User> allUsers = new ArrayList<>();

            for (int i = 0; i < mUserDataList.size(); i++) {
                allRepositories.addAll(getRepoListFromUserRes(mUserDataList.get(i)));
                allUsers.add(new User(mUserDataList.get(i), i));
            }

            DataManager dataManager = DataManager.getInstance();

            dataManager.getDaoSession()
                    .getRepositoryDao().insertOrReplaceInTx(allRepositories);
            dataManager.getDaoSession()
                    .getUserDao().insertOrReplaceInTx(allUsers);

            return null;
        }
    }

    /**
     * Описывает событие сохранения данных (для использования с Otto)
     */
    public class DataSavedEvent {
        boolean isSuccessful;

        public DataSavedEvent(boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
        }

        public boolean isSuccessful() {
            return isSuccessful;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mDataManager = DataManager.getInstance();

        bus = new Bus(ThreadEnforcer.MAIN);
        bus.register(this);

        if (getIntent().getBooleanExtra(ConstantManager.LOGIN_COMPLETE_KEY, false)) {
            saveUsersInDb();
        } else {
            splashStartDelay();
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DB_DATA_INS_LOADER:
                return new DbUserDataInsLoader(this);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.d(TAG, "Users data saved to database");
        bus.post(new DataSavedEvent(true));
    }

    @Override
    public void onLoaderReset(Loader loader) {}

    /**
     * Запускает Splash Screen с задержкой при старте приложения.
     * Затем запускает проверку токена авторизации.
     */
    private void splashStartDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkToken();
            }
        }, AppConfig.SPLASH_DELAY);
    }

    /**
     * Проверяет наличие токена авторизации в Shared Preferences
     * и переадресует в соответствующую активность (в зависимости от результата)
     */
    private void checkToken() {

        String authToken = mDataManager.getPreferencesManager().getAuthToken();

        if (authToken.isEmpty() || authToken.equals("null")) {
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
    }

    /**
     * Метод, подписанный на получение события DataSavedEvent по Otto Event Bus
     * (возникает, когда сохранение данных пользователей в БД выполнено)
     * @param event объект события
     */
    @Subscribe
    public void onSavedUserListData(DataSavedEvent event) {
        if (event.isSuccessful) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));;
        }
    }

    /**
     * Получает список пользователей с сервера и сохраняет его в БД,
     * используя Loader
     */
    private void saveUsersInDb() {
        Call<UserListRes> call = mDataManager.getUserList();

        showProgress();
        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                try {
                    if (response.code() == 200) {
                        mUserDataList = response.body().getData();

                        Loader loader = getSupportLoaderManager()
                                .initLoader(DB_DATA_INS_LOADER, null, SplashActivity.this);
                        loader.forceLoad();

                        hideProgress();
                    } else {
                        showToast(getString(R.string.msg_user_list_getting_error));
                        Log.e(TAG, "onResponse: " + String.valueOf(response.errorBody().source()));
                        hideProgress();
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG, e.toString());
                    showToast(getString(R.string.msg_smth_went_wrong));
                    hideProgress();
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                hideProgress();
                showToast(getString(R.string.msg_network_error));
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Возвращает список репозиториев пользователя.
     * @param userData данные пользователя, полученные с сервера
     * @return список репозиториев пользователя.
     */
    private static List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }

}
