package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.RepositoriesAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ListViewHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.util.List;

public class ProfileUserActivity extends BaseActivity {

    private Toolbar mToolbar;
    private ImageView mProfileImage;
    private EditText mUserBio;
    private TextView mUserRating, mUserCodeLines, mUserProjects;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    private ListView mRepoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mProfileImage = (ImageView) findViewById(R.id.user_photo_img);
        mUserBio = (EditText) findViewById(R.id.bio_et);
        mUserRating = (TextView) findViewById(R.id.user_rating_txt);
        mUserCodeLines = (TextView) findViewById(R.id.user_codelines_txt);
        mUserProjects = (TextView) findViewById(R.id.user_projects_txt);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);

        mRepoListView = (ListView) findViewById(R.id.repositories_list);
        setupToolbar();
        initProfileData();
    }

    /**
     * Инициализирует Toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Инициализирует профиль пользователя данными, полученными из активности UserListActivity
     */
    private void initProfileData() {
        UserDTO userDTO = getIntent().getParcelableExtra(ConstantManager.PARCELABLE_KEY);

        final List<String> repositories = userDTO.getRepositories();
        final RepositoriesAdapter repositoriesAdapter = new RepositoriesAdapter(this, repositories);
        mRepoListView.setAdapter(repositoriesAdapter);

        // устанавливает высоту списка исходя из общей высоты его элементов
        ListViewHelper.setListViewHeightBasedOnItems(mRepoListView);

        mRepoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browseIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://" + repositories.get(position)));
                startActivity(browseIntent);
            }
        });

        mUserBio.setText(userDTO.getBio());
        mUserRating.setText(userDTO.getRating());
        mUserCodeLines.setText(userDTO.getCodeLines());
        mUserProjects.setText(userDTO.getProjects());

        mCollapsingToolbarLayout.setTitle(userDTO.getFullName());

        final String userPhoto;
        if (userDTO.getPhoto().isEmpty()) {
            userPhoto = null;
            Log.e(TAG, "initProfileData: user with name " + userDTO.getFullName() + " has empty photo");
        } else {
            userPhoto = userDTO.getPhoto();
        }

        final int dummyPhotoId = R.drawable.user_bg;
        DataManager.getInstance().getPicasso()
                .load(userPhoto)
                .error(dummyPhotoId)
                .placeholder(dummyPhotoId)
                .fit()
                .centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, " load from cache");
                    }

                    @Override
                    public void onError() {
                        DataManager.getInstance().getPicasso()
                                .load(userPhoto)
                                .error(dummyPhotoId)
                                .placeholder(dummyPhotoId)
                                .fit()
                                .centerCrop()
                                .into(mProfileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {}

                                    @Override
                                    public void onError() {
                                        Log.d(TAG, "Could not fetch image");
                                    }
                                });
                    }
                });

//        String photoUrl = userDTO.getPhoto();
//        Picasso.with(this)
//                .load(photoUrl.isEmpty() ? null : photoUrl)
//                .placeholder(R.drawable.user_bg)
//                .error(R.drawable.user_bg)
//                .into(mProfileImage);
    }

}
