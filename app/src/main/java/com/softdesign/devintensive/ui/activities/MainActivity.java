package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UploadPhotoRes;
import com.softdesign.devintensive.utils.CircleTransform;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ValidateHelper;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "MainActivity";

    private DataManager mDataManager;
    private int mCurrentEditMode = ConstantManager.EDIT_MODE_OFF;

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.profile_placeholder) RelativeLayout mProfilePlaceholder;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img) ImageView mProfileImage;

    @BindView(R.id.call_img) ImageView mCallImg;
    @BindView(R.id.mail_img) ImageView mMailImg;
    @BindView(R.id.vk_img) ImageView mVkImg;
    @BindView(R.id.git_img) ImageView mGitImg;

    @BindView(R.id.phone_et) EditText mUserPhone;
    @BindView(R.id.email_et) EditText mUserMail;
    @BindView(R.id.vk_et) EditText mUserVk;
    @BindView(R.id.github_et) EditText mUserGit;
    @BindView(R.id.bio_et) EditText mUserBio;

    @BindView(R.id.phone_text_layout)
    TextInputLayout mUserPhoneLayout;
    @BindView(R.id.email_text_layout)
    TextInputLayout mUserMailLayout;
    @BindView(R.id.vk_text_layout)
    TextInputLayout mUserVkLayout;
    @BindView(R.id.github_text_layout)
    TextInputLayout mUserGitLayout;

    // в данном случае не лучший вариант (возможно инициирует повторный findById)
    @BindViews({R.id.phone_et, R.id.email_et, R.id.vk_et, R.id.github_et, R.id.bio_et})
    List<EditText> mUserInfoViews;

    @BindView(R.id.user_rating_txt) TextView mUserValueRating;
    @BindView(R.id.user_codelines_txt) TextView mUserValueCodeLines;
    @BindView(R.id.user_projects_txt) TextView mUserValueProjects;

    @BindViews({R.id.user_rating_txt, R.id.user_codelines_txt, R.id.user_projects_txt})
    List<TextView> mUserValueViews;

    private AppBarLayout.LayoutParams mAppBarParams = null;
    private File mPhotoFile = null;
    private Uri mSelectedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        mCallImg.setOnClickListener(this);
        mMailImg.setOnClickListener(this);
        mVkImg.setOnClickListener(this);
        mGitImg.setOnClickListener(this);

        mFab.setOnClickListener(this);
        mProfilePlaceholder.setOnClickListener(this);

        setupToolbar();
        setupDrawer();
        initUserFields();
        initUserInfoValues();

        initProfileImage();

        if (savedInstanceState == null) {
            // активность запускается впервые
        } else {
            // активность уже создавалась
            mCurrentEditMode = savedInstanceState
                    .getInt(ConstantManager.EDIT_MODE_KEY, ConstantManager.EDIT_MODE_OFF);
            changeEditMode(mCurrentEditMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (checkTokenEmpty()) {
            startActivity(new Intent(this, AuthActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (validateUserInfoValues(false)) {
            saveUserFields();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (mCurrentEditMode == ConstantManager.EDIT_MODE_OFF) {
                    showShackbar(getString(R.string.snackbar_msg_edit_on));
                    changeEditMode(ConstantManager.EDIT_MODE_ON);
                    mCurrentEditMode = ConstantManager.EDIT_MODE_ON;
                } else {
                    if (validateUserInfoValues(true)) {
                        showShackbar(getString(R.string.snackbar_msg_data_saved));
                        changeEditMode(ConstantManager.EDIT_MODE_OFF);
                        mCurrentEditMode = ConstantManager.EDIT_MODE_OFF;
                    }
                }
                break;
            case R.id.profile_placeholder:
                if (getPermissions()) {
                    showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                }
                break;
            case R.id.call_img:
                if (mCurrentEditMode == ConstantManager.EDIT_MODE_OFF) {
                    callPhone(mUserPhone.getText().toString());
                }
                break;
            case R.id.mail_img:
                if (mCurrentEditMode == ConstantManager.EDIT_MODE_OFF) {
                    sendMail(mUserPhone.getText().toString());
                }
                break;
            case R.id.vk_img:
                if (mCurrentEditMode == ConstantManager.EDIT_MODE_OFF) {
                    browseUrl("https://" + mUserVk.getText().toString());
                }
                break;
            case R.id.git_img:
                if (mCurrentEditMode == ConstantManager.EDIT_MODE_OFF) {
                    browseUrl("https://" + mUserGit.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawer != null && mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Отображает панель Shackbar, содержащую текстовое сообщение
     * @param message текст сообщения
     */
    private void showShackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Выполняет инициализацию панели Toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbar.getLayoutParams();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Выполняет инициализацию фотографии в профиле пользователя.
     * Загружает фотографию с сервера. В случае неудачи использует локальное изображение.
     */
    private void initProfileImage() {

        final String userPhotoUrl = mDataManager.getPreferencesManager().loadUserPhotoUrl();
        final int dummyPhotoId = R.drawable.user_bg;

        // пытаемся загрузить фотографию из сети
        DataManager.getInstance().getPicasso()
                .load(userPhotoUrl)
                .error(dummyPhotoId)
                .placeholder(dummyPhotoId)
                .fit()
                .centerCrop()
                .into(mProfileImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, " User photo loaded from network");
                    }

                    @Override
                    public void onError() {
                        // пытаемся загрузить фотографию из кеша
                        DataManager.getInstance().getPicasso()
                                .load(userPhotoUrl)
                                .error(dummyPhotoId)
                                .placeholder(dummyPhotoId)
                                .fit()
                                .centerCrop()
                                .transform(new CircleTransform())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(mProfileImage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, " User photo loaded from cash");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.d(TAG, " Could not fetch user photo");
                                    }
                                });
                    }
                });
    }


    /**
     * Загружает фотографию из профиля пользователя на сервер
     * @param photoFile представление файла фотографии
     */
    private void uploadPhoto(File photoFile) {

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), photoFile);
        MultipartBody.Part bodyPart =
                MultipartBody.Part.createFormData("photo", photoFile.getName(), requestBody);

        Call<UploadPhotoRes> call = mDataManager.uploadPhoto(
                mDataManager.getPreferencesManager().getUserId(), bodyPart);
        call.enqueue(new Callback<UploadPhotoRes>() {
            @Override
            public void onResponse(Call<UploadPhotoRes> call, Response<UploadPhotoRes> response) {
                if (response.body().isSuccess()) {
                    Log.d("TAG", "Upload success");
                } else {
                    showShackbar(getString(R.string.snackbar_msg_photo_upload_error));
                }
            }

            @Override
            public void onFailure(Call<UploadPhotoRes> call, Throwable t) {
                showShackbar(getString(R.string.snackbar_msg_photo_upload_error));
                Log.e("Upload error", t.getMessage());
            }
        });
    }

    /**
     * Выполняет инициализацию выдвижной панели навигационного меню
     */
    private void setupDrawer() {
        if (mNavigationView != null) {

            View headerView = mNavigationView.getHeaderView(0);

            List<String> userNames = mDataManager.getPreferencesManager().loadUserFullName();
            ((TextView)headerView.findViewById(R.id.user_name_txt))
                    .setText(String.format("%s %s", userNames.get(1), userNames.get(0)));
            ((TextView)headerView.findViewById(R.id.user_email_txt))
                    .setText(mDataManager.getPreferencesManager().getUserLogin());

            updateAvatar();

            mNavigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem item) {
                            mNavigationDrawer.closeDrawer(GravityCompat.START);
                            switch (item.getItemId()) {
                                case R.id.team_menu:
                                    startActivity(new Intent(MainActivity.this, UserListActivity.class));
                                    return true;
                                case R.id.exit_menu:
                                    logout();
                                    return true;
                            }
                            return false;
                        }
                    });
        }
    }

    /**
     * Устанавливает аватар пользователя на выдвижной панеле.
     * Загружает аватар с сервера. В случае неудачи использует изображение из кэша.
     * Для отображения аватара используется скругление.
     */
    public void updateAvatar() {

        final ImageView avatarImg = (ImageView)
                mNavigationView.getHeaderView(0).findViewById(R.id.avatar);

        final String userAvatarUrl = mDataManager.getPreferencesManager().loadUserAvatarUrl();
        final int dummyAvatarId = R.drawable.no_avatar;

        // пытаемся загрузить аватар из сети
        DataManager.getInstance().getPicasso()
                .load(userAvatarUrl)
                .error(dummyAvatarId)
                .placeholder(dummyAvatarId)
                .fit()
                .centerCrop()
                .transform(new CircleTransform())
                .into(avatarImg, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, " User avatar loaded from network");
                    }

                    @Override
                    public void onError() {
                        // пытаемся загрузить аватар из кеша
                        DataManager.getInstance().getPicasso()
                                .load(userAvatarUrl)
                                .error(dummyAvatarId)
                                .placeholder(dummyAvatarId)
                                .fit()
                                .centerCrop()
                                .transform(new CircleTransform())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(avatarImg, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, " User avatar loaded from cash");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.d(TAG, " Could not fetch user avatar");
                                    }
                                });
                    }
                });
    }


    /**
     * Получение результата из другой Activity (фото из камеры или галлереи)
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mSelectedImage = data.getData();

                    insertProfileImage(mSelectedImage);
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mSelectedImage = Uri.fromFile(mPhotoFile);

                    insertProfileImage(mSelectedImage);
                }
                break;
        }
    }

    /**
     * Переключает режим редактирования
     * @param mode если 1 - режим редактирования, если 0 - режим просмотра
     */
    private void changeEditMode(int mode) {
        if (mode == ConstantManager.EDIT_MODE_ON) {
            mFab.setImageResource(R.drawable.ic_done_white_24dp);

            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(true);
                userValue.setFocusable(true);
                userValue.setFocusableInTouchMode(true);
            }
            setFieldFocus(mUserPhone, false);

            showProfilePlaceholder();
            lockToolbar();
            mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);

        } else {
            mFab.setImageResource(R.drawable.ic_create_white_24dp);

            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(false);
                userValue.setFocusable(false);
                userValue.setFocusableInTouchMode(false);
            }

            hideProfilePlaceholder();
            unlockToolbar();
            mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.white));

            saveUserFields();
        }
    }

    /**
     * Осуществляет валидацию полей профайла пользователя. Отображает соответствующие сообщения.
     * @param showMsg разрешить (запретить) отображение сообщений об ошибках валидации
     * @return true, если валидация пройдена и false - в противном случае
     */
    private boolean validateUserInfoValues(boolean showMsg) {

        String userPhone = ValidateHelper.getValidatedPhone(mUserPhone.getText().toString());
        if (userPhone != null) {
            mUserPhone.setText(ValidateHelper.formatRegularPhone(userPhone));
            mUserPhoneLayout.setErrorEnabled(false);
        } else {
            if (showMsg) {
                setFieldFocus(mUserPhone, true);
                mUserPhoneLayout.setError(getString(R.string.validation_msg_phone));
            }
            return false;
        }

        String userMail = ValidateHelper.getValidatedEmail(mUserMail.getText().toString());
        if (userMail != null) {
            mUserMail.setText(userMail);
            mUserMailLayout.setErrorEnabled(false);
        } else {
            if (showMsg) {
                setFieldFocus(mUserMail, true);
                mUserMailLayout.setError(getString(R.string.validation_msg_email));
            }
            return false;
        }

        String userVk = ValidateHelper.getValidatedVkUrl(mUserVk.getText().toString());
        if (userVk != null) {
            mUserVk.setText(userVk);
            mUserVkLayout.setErrorEnabled(false);
        } else {
            if (showMsg) {
                setFieldFocus(mUserVk, true);
                mUserVkLayout.setError(getString(R.string.validation_msg_vk));
            }
            return false;
        }

        String userGit = ValidateHelper.getValidatedGitUrl(mUserGit.getText().toString());
        if (userGit != null) {
            mUserGit.setText(userGit);
            mUserGitLayout.setErrorEnabled(false);
        } else {
            if (showMsg) {
                setFieldFocus(mUserGit, true);
                mUserGitLayout.setError(getString(R.string.validation_msg_git));
            }
            return false;
        }

        return true;
    }

    /**
     * Устанавливает фокус ввода на представление, заданное параметром
     * @param view представление, принимающее фокус
     * @param hideKeyboard запретить (разрешить) отображение клавиатуры
     */
    private void setFieldFocus(View view, boolean hideKeyboard) {
        if (view != null) {
            view.requestFocus();
            if (hideKeyboard) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mUserMail.getWindowToken(), 0);
            }
        }
    }

    /**
     * Загружает пользовательские данные в поля профайла
     */
    private void initUserFields() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfoViews.get(i).setText(userData.get(i));
        }
    }

    /**
     * Сохраняет пользовательские данные, введенные в поля профайла
     */
    private void saveUserFields() {
        List<String> userData = new ArrayList<>();
        for (EditText userFieldView : mUserInfoViews) {
            userData.add(userFieldView.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

    /**
     * Загружает данные пользователя (рейтинг, строки кода, проекта) из Shared Preferences
     * в текстовые поля заголовка профиля
     */
    private void initUserInfoValues() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileValues();
        for (int i = 0; i < userData.size(); i++) {
            mUserValueViews.get(i).setText(userData.get(i));
        }
    }

    /**
     * Запускает получение фотографии из галлереи
     */
    private void loadPhotoFromGallery() {
        Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        takeGalleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(takeGalleryIntent,
                getString(R.string.user_profile_chose_message)),
                ConstantManager.REQUEST_GALLERY_PICTURE);
    }

    /**
     * Обеспечивает получение разрешений на использование камеры и внешнего хранилища
     * (для Android 6)
     * @return true - если разрешения были ранее установлены и false - в противном случае
     */
    private boolean getPermissions() {
        // проверка разрешений для Android 6
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        } else {
            // запрос разрешений на использование камеры и внешнего хранилища данных
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_REQUEST_PERMISSION_CODE);

            // предоставление пользователю возможности установить разрешения,
            // если он ранее запретил их и выбрал опцию "не показывать больше"
            Snackbar.make(mCoordinatorLayout, R.string.snackbar_msg_permissions_request, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_allow_permissions, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
        return false;
    }

    /**
     * Запускает получение фотографии из камеры
     */
    private void loadPhotoFromCamera() {

        Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            mPhotoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
            showShackbar(getString(R.string.snackbar_msg_file_creating_error));
        }
        if (mPhotoFile != null) {
            takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
            startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.CAMERA_REQUEST_PERMISSION_CODE
                && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showShackbar(getString(R.string.snackbar_msg_permission_camera_granted));
            }
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showShackbar(getString(R.string.snackbar_msg_permission_storage_granted));
            }
        }
    }

    /**
     * Скрывает плейсхолдер фотографии в профайле пользователя
     */
    private void hideProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.GONE);
    }

    /**
     * Отображает плейсхолдер фотографии в профайле пользователя
     */
    private void showProfilePlaceholder() {
        mProfilePlaceholder.setVisibility(View.VISIBLE);
    }

    /**
     * Блокирует сворачивание Collapsing Toolbar
     */
    private void lockToolbar() {
        mAppBarLayout.setExpanded(true, false);
        // сбрасывает флаги скроллинга
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    /**
     * Разблокирует сворачивание Collapsing Toolbar
     */
    private void unlockToolbar() {
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectItems = {
                        getString(R.string.user_profile_dialog_gallery),
                        getString(R.string.user_profile_dialog_camera),
                        getString(R.string.user_profile_dialog_cancel)
                };

                AlertDialog.Builder builder =  new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.user_profile_dialog_title));
                builder.setItems(selectItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiceItem) {
                        switch (choiceItem) {
                            case 0:
                                loadPhotoFromGallery();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();

            default:
                return null;
        }
    }

    /**
     * Создает файл с уникальным именем для хранения фотографии во внешнем хранилище
     * @return объект {@link File} для созданного файла
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Android 6 ???: сохранение изображения с добавлением в галлерею
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * Помещает в профиль пользователя фотографию с заданным URI,
     * сохраняет URI фотографии в Shared Preferences,
     * загружает фотографию на сервер, если URI изменился
     * @param selectedImage URI изображения
     */
    private void insertProfileImage(Uri selectedImage) {

        Picasso.with(this)
                .load(selectedImage)
                .into(mProfileImage);

        // если URI фотографии изменился
        if (! selectedImage.equals(mDataManager.getPreferencesManager().loadUserPhoto())) {

            if (selectedImage.getLastPathSegment().endsWith(".jpg")) {
                uploadPhoto(new File(selectedImage.getPath()));
            } else {
                uploadPhoto(new File(getPath(selectedImage)));
            }

            mDataManager.getPreferencesManager().saveUserPhoto(selectedImage);
        }

    }

    /**
     * Возвращает путь к файлу для заданного URI
     * (используется для корректной выгрузки на сервер при выборе файла в галерее)
     * @param uri URI файла
     * @return путь к файлу
     */
    @Nullable
    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    /**
     * Открывает настройки приложения
     */
    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(appSettingsIntent, ConstantManager.PERMISSION_REQUEST_SETTINGS_CODE);
    }


    /**
     * Инициирует телефонный звонок на заданный номер
     * @param phoneStr номер телефона
     */
    private void callPhone(String phoneStr) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneStr));
        startActivity(dialIntent);
    }

    /**
     * Инициирует отправку письма по электронной почте
     * @param email адрес электронной почты
     */
    private void sendMail(String email) {
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(mailIntent, getString(R.string.chooser_title_send_mail)));
    }

    /**
     * Открывает ссылку в браузере по заданному URL-адресу
     * @param url URL-адрес
     */
    private void browseUrl(String url) {
        Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browseIntent);
    }

}
