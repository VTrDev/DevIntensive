package com.softdesign.devintensive.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.SuggestsAdapter;
import com.softdesign.devintensive.ui.adapters.SuggestsAdapter.SuggestModel;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.CircleTransform;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + " UserListActivity";

    private static final int DB_USERS_LOADER = 0;
    private static final int DB_USERS_SAVE_STATE_LOADER = 1;

    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mNavigationDrawer;
    private NavigationView mNavigationView;

    private DataManager mDataManager;

    private RecyclerView mRecyclerView;
    private UsersAdapter mUsersAdapter;
    private static List<User> mUsers;

    private LinearLayout mSearchLayout;
    private SearchView mSearchView;
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

    /**
     * Loader для асинхронного сохранения состояния списка пользователей в БД
     */
    private static class DbUsersSaveStateLoader extends AsyncTaskLoader<Object> {

        public DbUsersSaveStateLoader(Context context) {
            super(context);
        }

        @Override
        public Object loadInBackground() {
            saveUserListStateToDb();
            return null;
        }
    }

    /**
     * Вспомогательный класс для реализации удаления (свайпом) и перетаскивания
     * элементов списка пользователей
     */
    private class UserItemTouchHelper extends ItemTouchHelper.SimpleCallback {

        private UsersAdapter mUsersAdapter;

        public UserItemTouchHelper(UsersAdapter usersAdapter) {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.START | ItemTouchHelper.END);
            this.mUsersAdapter = usersAdapter;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            int itemPos = viewHolder.getAdapterPosition();
            int targetPos = target.getAdapterPosition();
            mUsersAdapter.swap(itemPos, targetPos);
            updateSuggests();
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            mUsersAdapter.remove(position);
            updateSuggests();
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
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        //mSuggestsView = (CardView) findViewById(R.id.search_card);
        mSearchLayout = (LinearLayout) findViewById(R.id.search_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        setupToolbar();
        setupDrawer();

        getSupportLoaderManager().initLoader(DB_USERS_LOADER, null, this).forceLoad();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUsers = mUsersAdapter.getUsers();

        getSupportLoaderManager().initLoader(DB_USERS_SAVE_STATE_LOADER, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkTokenEmpty()) {
            startActivity(new Intent(this, AuthActivity.class));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint(getString(R.string.search_hint_input_username));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showSuggestList();
                } else {
                    hideSuggestList();
                    hideKeyboard(mSearchView);
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
            case DB_USERS_SAVE_STATE_LOADER:
                return new DbUsersSaveStateLoader(this);
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

    @Override
    public void onBackPressed() {
        if (mNavigationDrawer != null && mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Скрывает экранную клавиатуру, связанную с заданным виджетом.
     * @param view виджет
     */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Показывает список рекомендаций при поиске пользователя
     */
    private void showSuggestList() {
        if (mSearchLayout.getVisibility() == View.GONE) {
            mRecyclerView.setVisibility(View.GONE);
            mSearchLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setLayoutFrozen(true);
        }
    }

    /**
     * Скрывает список рекомендаций при поиске
     */
    private void hideSuggestList() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mSearchLayout.setVisibility(View.GONE);
        mRecyclerView.setLayoutFrozen(false);
    }

    /**
     * Выполняет инициализацию списка пользователей в RecyclerView
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

        ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new UserItemTouchHelper(mUsersAdapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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
                hideKeyboard(mSearchView);
                mRecyclerView.scrollToPosition(mSuggestsAdapter.getModel(position).getPosition());
            }
        });
        mSuggestsRecyclerView.setAdapter(mSuggestsAdapter);
    }

    /**
     * Выполняет обновление списка рекомендаций при поиске пользователя
     */
    private void updateSuggests() {
        mSearchSuggests.clear();
        List<User> allUsers = mUsersAdapter.getUsers();
        for (int i = 0; i < allUsers.size(); i++) {
            mSearchSuggests.add(new SuggestModel(allUsers.get(i).getFullName(), i));
        }
        mSuggestsAdapter.update(mSearchSuggests);
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
                                case R.id.user_profile_menu:
                                    startActivity(new Intent(UserListActivity.this, MainActivity.class));
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
     * Загружает аватар из кэеша. В случае неудачи загружает аватар из сети.
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
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(avatarImg, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, " User avatar loaded from cash");
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
                                .into(avatarImg, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, " User avatar loaded from network");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.d(TAG, " Could not fetch user avatar");
                                    }
                                });
                    }
                });
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    /**
     * Сохраняет состояние списка пользователей в БД
     */
    private static void saveUserListStateToDb() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(mUsers);

        for (int i = 0; i < allUsers.size(); i++) {
            allUsers.get(i).setPosition(i);
        }

        DataManager dataManager = DataManager.getInstance();
        dataManager.getDaoSession().getUserDao().deleteAll();
        dataManager.getDaoSession().getUserDao().insertOrReplaceInTx(allUsers);
    }

}
