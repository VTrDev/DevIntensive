package com.softdesign.devintensive.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.SuggestsAdapter;
import com.softdesign.devintensive.ui.adapters.SuggestsAdapter.SuggestModel;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.ConstantManager;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + " UserListActivity";
    private static final int DB_USERS_LOADER = 0;

    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mNavigationDrawer;

    private DataManager mDataManager;

    private RecyclerView mRecyclerView;
    private UsersAdapter mUsersAdapter;
    private List<User> mUsers;

    private CardView mSuggestsView;
    private RecyclerView mSuggestsRecyclerView;
    private SuggestsAdapter mSuggestsAdapter;
    private List<SuggestModel> mSearchSuggests = new ArrayList<>();

    /**
     * Loader для асинхронной загрузки данных пользователей из БД
     */
    private static class DbUsersLoader extends AsyncTaskLoader<List<User>> {

        public DbUsersLoader(Context context) {
            super(context);
        }

        @Override
        public List<User> loadInBackground() {
            return DataManager.getInstance().getUserListFromDb();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mDataManager = DataManager.getInstance();
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer);

        mSuggestsView = (CardView) findViewById(R.id.search_card);
        mRecyclerView = (RecyclerView) findViewById(R.id.user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        setupToolbar();
        setupDrawer();

        getSupportLoaderManager().initLoader(DB_USERS_LOADER, null, this).forceLoad();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint_input_username));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mSuggestsAdapter != null) {
                    showSuggestList();
                    mSuggestsAdapter.filter(newText);
                    mSuggestsRecyclerView.scrollToPosition(0);
                }
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showSuggestList();
                } else {
                    hideSuggestList();
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DB_USERS_LOADER:
                return new DbUsersLoader(this);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case DB_USERS_LOADER:
                if (data != null) {
                    mUsers = (List<User>)data;
                    if (mUsers.size() == 0) {
                        showSnackbar(getString(R.string.user_list_load_error));
                    } else {
                        Log.d(TAG, "Data is loaded");
                        setupUserList();
                        initSearchSuggests();
                    }
                }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Показывает список рекомендаций при поиске пользователя
     */
    private void showSuggestList() {
        if (mSuggestsView.getVisibility() == View.GONE) {
            mSuggestsView.setVisibility(View.VISIBLE);
            mRecyclerView.setLayoutFrozen(true);
        }
    }

    /**
     * Скрывает список рекомендаций при поиске
     */
    private void hideSuggestList() {
        mSuggestsView.setVisibility(View.GONE);
        mRecyclerView.setLayoutFrozen(false);
    }

    /**
     * Выполняет инициализацию списка пользователей
     */
    private void setupUserList() {
        mUsersAdapter = new UsersAdapter(mUsers, new UsersAdapter.UserViewHolder.CustomClickListener() {
            @Override
            public void onUserItemClickListener(int position) {
                UserDTO userDTO = new UserDTO(mUsers.get(position));
                Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);

                startActivity(profileIntent);
            }
        });

        mRecyclerView.setAdapter(mUsersAdapter);
    }

    /**
     * Выполняет инициализацию списка рекомендаций при поиске пользователя
     */
    private void initSearchSuggests() {
        for (int i = 0; i < mUsers.size(); i++) {
            mSearchSuggests.add(new SuggestModel(mUsers.get(i).getFullName(), i));
        }

        mSuggestsRecyclerView = (RecyclerView) findViewById(R.id.search_suggests_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mSuggestsRecyclerView.setLayoutManager(layoutManager);
        mSuggestsAdapter = new SuggestsAdapter(mSearchSuggests,
                new SuggestsAdapter.SuggestViewHolder.OnSuggestionClickListener() {
            @Override
            public void onSuggestionClickListener(int position) {
                hideSuggestList();
                mRecyclerView.scrollToPosition(mSuggestsAdapter.getModel(position).getPosition());
            }
        });
        mSuggestsRecyclerView.setAdapter(mSuggestsAdapter);
    }

    private void setupDrawer() {
        // TODO: 14.07.2016 Реализовать переход в другую активность при клике по элементу меню в NavigationDrawer
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

}
