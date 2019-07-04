package com.app.tvio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.adapter.NavAdapter;
import com.app.fragment.AllChannelFragment;
import com.app.fragment.CategoryFragment;
import com.app.fragment.FavouriteFragment;
import com.app.fragment.FeaturedFragment;
import com.app.fragment.HomeFragment;
import com.app.fragment.LatestFragment;
import com.app.fragment.SearchFragment;
import com.app.fragment.VideoFragment;
import com.app.item.ItemNav;
import com.app.util.BannerAds;
import com.app.util.Constant;
import com.app.util.IsRTL;
import com.app.util.NetworkUtils;
import com.app.util.RecyclerTouchListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ixidev.gdpr.GDPRChecker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    public NavAdapter navAdapter;
    private FragmentManager fragmentManager;
    ArrayList<ItemNav> mNavItem;
    BottomNavigationView navigation;
    DrawerLayout drawer;
    MyApplication MyApp;
    TextView textName, textEmail;
    int previousSelect = 0;
    boolean doubleBackToExitPressedOnce = false;
    LinearLayout mAdViewLayout;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    HomeFragment homeFragment = new HomeFragment();
                    navigationItemSelected(0);
                    loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
                    return true;
                case R.id.navigation_tv:
                    AllChannelFragment allChannelFragment = new AllChannelFragment();
                    navigationItemSelected(0);
                    loadFrag(allChannelFragment, getString(R.string.menu_live_tv), fragmentManager);
                    return true;
                case R.id.navigation_video:
                    VideoFragment videoFragment = new VideoFragment();
                    navigationItemSelected(4);
                    loadFrag(videoFragment, getString(R.string.menu_video), fragmentManager);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IsRTL.ifSupported(MainActivity.this);

        //initToolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdViewLayout = findViewById(R.id.adView);
        fragmentManager = getSupportFragmentManager();
        MyApp = MyApplication.getInstance();

        //Bottom Navigation
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavItem = new ArrayList<>();
        fillNavItem();
        textName = findViewById(R.id.nav_name);
        textEmail = findViewById(R.id.nav_email);
        RecyclerView recyclerView = findViewById(R.id.navigation_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
        navAdapter = new NavAdapter(MainActivity.this, mNavItem);
        recyclerView.setAdapter(navAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                navigationClick(mNavItem.get(position).getId());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        HomeFragment homeFragment = new HomeFragment();
        loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);

        if (NetworkUtils.isConnected(MainActivity.this)) {
            getAppConsent();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                // TODO Auto-generated method stub
                hideShowBottomView(false);
                String categoryName = getString(R.string.menu_search);
                Bundle bundle = new Bundle();
                bundle.putString("search", arg0);

                SearchFragment searchFragment = new SearchFragment();
                searchFragment.setArguments(bundle);
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.hide(fragmentManager.findFragmentById(R.id.Container));
                ft.add(R.id.Container, searchFragment, categoryName);
                ft.addToBackStack(categoryName);
                ft.commit();
                setToolbarTitle(categoryName);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void navigationClick(int position) {
        drawer.closeDrawers();
        switch (position) {
            case 0:
                navigationItemSelected(0);
                navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
                HomeFragment homeFragment = new HomeFragment();
                loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
                hideShowBottomView(true);
                break;
            case 1:
                navigationItemSelected(1);
                LatestFragment latestFragment = new LatestFragment();
                loadFrag(latestFragment, getString(R.string.menu_latest), fragmentManager);
                hideShowBottomView(false);
                break;
            case 2:
                navigationItemSelected(2);
                CategoryFragment categoryFragment = new CategoryFragment();
                loadFrag(categoryFragment, getString(R.string.menu_category), fragmentManager);
                hideShowBottomView(false);
                break;
            case 3:
                navigationItemSelected(3);
                FeaturedFragment featuredFragment = new FeaturedFragment();
                loadFrag(featuredFragment, getString(R.string.menu_featured), fragmentManager);
                hideShowBottomView(false);
                break;
            case 4:
                navigation.getMenu().findItem(R.id.navigation_video).setChecked(true);
                navigationItemSelected(4);
                VideoFragment videoFragment = new VideoFragment();
                loadFrag(videoFragment, getString(R.string.menu_video), fragmentManager);
                hideShowBottomView(true);
                break;
            case 5:
                navigationItemSelected(5);
                FavouriteFragment favouriteFragment = new FavouriteFragment();
                loadFrag(favouriteFragment, getString(R.string.menu_favourite), fragmentManager);
                hideShowBottomView(false);
                break;
            case 10:
                navAdapter.setSelected(previousSelect);
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case 11:
                Logout();
                break;
            case 12:
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case 13:
                navigationItemSelected(previousSelect);
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
        }
    }

    private void fillNavItem() {
        mNavItem.add(new ItemNav(0, R.drawable.ic_home_drawer, getResources().getString(R.string.menu_home)));
        mNavItem.add(new ItemNav(1, R.drawable.ic_latest_drawer, getResources().getString(R.string.menu_latest)));
        mNavItem.add(new ItemNav(2, R.drawable.ic_category_drawer, getResources().getString(R.string.menu_category)));
        mNavItem.add(new ItemNav(3, R.drawable.ic_featured_drawer, getResources().getString(R.string.menu_featured)));
        mNavItem.add(new ItemNav(4, R.drawable.ic_video_drawer, getResources().getString(R.string.menu_video)));
        mNavItem.add(new ItemNav(5, R.drawable.ic_favorit_drawer, getResources().getString(R.string.menu_favourite)));
        mNavItem.add(new ItemNav(13, R.drawable.ic_setting_drawer, getResources().getString(R.string.menu_setting)));
        if (MyApp.getIsLogin()) {
            mNavItem.add(new ItemNav(10, R.drawable.ic_profile_drawer, getResources().getString(R.string.menu_profile)));
            mNavItem.add(new ItemNav(11, R.drawable.ic_logout_drawer, getResources().getString(R.string.menu_logout)));
        } else {
            mNavItem.add(new ItemNav(12, R.drawable.ic_login_drawer, getResources().getString(R.string.login)));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        View v1 = findViewById(R.id.view_fake);
        v1.requestFocus();
        if (MyApp.getIsLogin()) {
            textName.setText(MyApp.getUserName());
            textEmail.setText(MyApp.getUserEmail());
        }
    }

    private void Logout() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.menu_logout))
                .setMessage(getString(R.string.logout_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyApp.saveIsLogin(false);
                        Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_exit_to_app_white)
                .show();
    }

    private void getAppConsent() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constant.API_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    Constant.isBanner = objJson.getBoolean("banner_ad");
                    Constant.isInterstitial = objJson.getBoolean("interstital_ad");
                    Constant.adMobBannerId = objJson.getString("banner_ad_id");
                    Constant.adMobInterstitialId = objJson.getString("interstital_ad_id");
                    Constant.adMobPublisherId = objJson.getString("publisher_id");
                    Constant.AD_COUNT_SHOW = objJson.getInt("interstital_ad_click");

                    new GDPRChecker()
                            .withContext(MainActivity.this)
                            .withPrivacyUrl(getString(R.string.privacy_url)) // your privacy url
                            .withPublisherIds(Constant.adMobPublisherId) // your admob account Publisher id
                            .withTestMode("9424DF76F06983D1392E609FC074596C") // remove this on real project
                            .check();

                    BannerAds.ShowBannerAds(MainActivity.this, mAdViewLayout);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }

        });
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        FragmentTransaction ft = fm.beginTransaction();
        //  ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.Container, f1, name);
        ft.commit();
        setToolbarTitle(name);
    }

    public void hideShowBottomView(boolean visibility) {
        navigation.setVisibility(visibility ? View.VISIBLE : View.GONE);
        mAdViewLayout.setVisibility(visibility ? View.GONE : View.VISIBLE);
    }

    public void navigationItemSelected(int position) {
        previousSelect = position;
        navAdapter.setSelected(position);
    }

    public void setToolbarTitle(String Title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Title);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragmentManager.getBackStackEntryCount() != 0) {
            String tag = fragmentManager.getFragments().get(fragmentManager.getBackStackEntryCount() - 1).getTag();
            setToolbarTitle(tag);
            //when search is click and goes back if home
            assert tag != null;
            if (tag.equals(getString(R.string.menu_home)) || tag.equals(getString(R.string.menu_live_tv)) || tag.equals(getString(R.string.menu_video))) {
                hideShowBottomView(true);
            }
            super.onBackPressed();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Klik BACK sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}
