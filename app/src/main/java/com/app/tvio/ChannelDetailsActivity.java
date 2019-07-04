package com.app.tvio;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.adapter.RelatedAdapter;
import com.app.cast.Casty;
import com.app.cast.MediaData;
import com.app.db.DatabaseHelper;
import com.app.fragment.CommentFragment;
import com.app.item.ItemChannel;
import com.app.util.BannerAds;
import com.app.util.Constant;
import com.app.util.IsRTL;
import com.app.util.ItemOffsetDecoration;
import com.app.util.NetworkUtils;
import com.github.ornolfr.ratingview.RatingView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChannelDetailsActivity extends AppCompatActivity {

    ImageView imageChannel, imagePlay, imageRate;
    TextView textChannelName, textChannelCategory, textRate;
    Button btnReportChannel;
    RatingView ratingView;
    ProgressBar mProgressBar;
    ScrollView mScrollView;
    WebView webView;
    LinearLayout lyt_not_found;
    ItemChannel objBean;
    String Id;
    ProgressDialog pDialog;
    ArrayList<ItemChannel> mListItemRelated;
    RecyclerView mRecyclerView;
    RelatedAdapter relatedAdapter;
    private FragmentManager fragmentManager;
    boolean isFromNotification = false;
    Menu menu;
    DatabaseHelper databaseHelper;
    private Casty casty;
    MyApplication myApp;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        IsRTL.ifSupported(ChannelDetailsActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        LinearLayout mAdViewLayout = findViewById(R.id.adView);
        BannerAds.ShowBannerAds(getApplicationContext(), mAdViewLayout);
        casty = Casty.create(this)
                .withMiniController();
        myApp = MyApplication.getInstance();
        databaseHelper = new DatabaseHelper(getApplicationContext());
        fragmentManager = getSupportFragmentManager();
        mListItemRelated = new ArrayList<>();
        pDialog = new ProgressDialog(this);
        imageChannel = findViewById(R.id.image);
        imagePlay = findViewById(R.id.imagePlay);
        imageRate = findViewById(R.id.imageEditRate);
        textChannelName = findViewById(R.id.text);
        textRate = findViewById(R.id.textRate);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        textChannelCategory = findViewById(R.id.textCategory);
        ratingView = findViewById(R.id.ratingView);
        mProgressBar = findViewById(R.id.progressBar1);
        mScrollView = findViewById(R.id.scrollView);
        webView = findViewById(R.id.webView);
        btnReportChannel = findViewById(R.id.btn_report_channel);
        mRecyclerView = findViewById(R.id.rv_relate_channel);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ChannelDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ChannelDetailsActivity.this, R.dimen.item_offset);
        mRecyclerView.addItemDecoration(itemDecoration);

        objBean = new ItemChannel();
        webView.setBackgroundColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        Id = intent.getStringExtra("Id");
        if (intent.hasExtra("isNotification")) {
            isFromNotification = true;
        }

        if (NetworkUtils.isConnected(ChannelDetailsActivity.this)) {
            getChannelDetails();
        } else {
            showToast(getString(R.string.conne_msg1));
        }

        imageRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApp.getIsLogin()) {
                    showRating();
                } else {
                    showToast(getString(R.string.login_msg));
                }
            }
        });

        btnReportChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChannelDetailsActivity.this, ReportChannelActivity.class);
                intent.putExtra("Id", Id);
                intent.putExtra("cName", objBean.getChannelName());
                intent.putExtra("cCategory", objBean.getChannelCategory());
                intent.putExtra("cImage", objBean.getImage());
                intent.putExtra("cRate", objBean.getChannelAvgRate());
                startActivity(intent);
            }
        });

        imagePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (casty.isConnected()) {
                    playViaCast();
                } else {
                    if (objBean.isTv()) {
                        if (myApp.getExternalPlayer()) {
                            showExternalPlay();
                        } else {
                            Intent intent = new Intent(ChannelDetailsActivity.this, TVPlayActivity.class);
                            intent.putExtra("videoUrl", objBean.getChannelUrl());
                            startActivity(intent);
                        }
                    } else {
                        String videoId = NetworkUtils.getVideoId(objBean.getChannelUrl());
                        Intent intent = new Intent(ChannelDetailsActivity.this, YtPlayActivity.class);
                        intent.putExtra("id", videoId);
                        startActivity(intent);
                    }
                }
            }
        });

    }

    private void getChannelDetails() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("get_single_channel_id", Id);
        client.get(Constant.API_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBar.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBar.setVisibility(View.GONE);
                mScrollView.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    if (jsonArray.length() > 0) {
                        JSONObject objJson;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            objBean.setId(objJson.getString(Constant.CHANNEL_ID));
                            objBean.setChannelName(objJson.getString(Constant.CHANNEL_TITLE));
                            objBean.setChannelCategory(objJson.getString(Constant.CATEGORY_NAME));
                            objBean.setImage(objJson.getString(Constant.CHANNEL_IMAGE));
                            objBean.setChannelAvgRate(objJson.getString(Constant.CHANNEL_AVG_RATE));
                            objBean.setDescription(objJson.getString(Constant.CHANNEL_DESC));
                            objBean.setChannelUrl(objJson.getString(Constant.CHANNEL_URL));
                            objBean.setIsTv(objJson.getString(Constant.CHANNEL_TYPE).equals("live_url"));

                            JSONArray jsonArrayChild = objJson.getJSONArray(Constant.RELATED_ITEM_ARRAY_NAME);
                            if (jsonArrayChild.length() != 0) {
                                for (int j = 0; j < jsonArrayChild.length(); j++) {
                                    JSONObject objChild = jsonArrayChild.getJSONObject(j);
                                    ItemChannel item = new ItemChannel();
                                    item.setId(objChild.getString(Constant.RELATED_ITEM_CHANNEL_ID));
                                    item.setChannelName(objChild.getString(Constant.RELATED_ITEM_CHANNEL_NAME));
                                    item.setImage(objChild.getString(Constant.RELATED_ITEM_CHANNEL_THUMB));
                                    mListItemRelated.add(item);
                                }
                            }
                        }
                        displayData();

                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mProgressBar.setVisibility(View.GONE);
                mScrollView.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }

        });
    }

    private void displayData() {
        Picasso.get().load(objBean.getImage()).placeholder(R.drawable.placeholder).into(imageChannel);
        setTitle(objBean.getChannelName());
        textChannelName.setText(objBean.getChannelName());
        textChannelCategory.setText(objBean.getChannelCategory());
        textRate.setText(objBean.getChannelAvgRate());
        ratingView.setRating(Float.parseFloat(objBean.getChannelAvgRate()));
        String mimeType = "text/html";
        String encoding = "utf-8";
        String htmlText = objBean.getDescription();

        boolean isRTL = Boolean.parseBoolean(getResources().getString(R.string.isRTL));
        String direction = isRTL ? "rtl" : "ltr";

        String text = "<html dir=" + direction + "><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom.otf\")}body{font-family: MyFont;color: #a5a5a5;text-align:left;font-size:15px;margin-left:0px;line-height:1.2}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

        if (!mListItemRelated.isEmpty()) {
            relatedAdapter = new RelatedAdapter(ChannelDetailsActivity.this, mListItemRelated);
            mRecyclerView.setAdapter(relatedAdapter);
        }

        CommentFragment commentFragment = CommentFragment.newInstance(Id);
        fragmentManager.beginTransaction().replace(R.id.comment_container, commentFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        casty.addMediaRouteMenuItem(menu);
        getMenuInflater().inflate(R.menu.menu_details, menu);
        this.menu = menu;
        isFavourite();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_favourite:
                ContentValues fav = new ContentValues();
                if (databaseHelper.getFavouriteById(Id)) {
                    databaseHelper.removeFavouriteById(Id);
                    menu.findItem(R.id.menu_favourite).setIcon(R.drawable.favorite_normal);
                    Toast.makeText(ChannelDetailsActivity.this, getString(R.string.favourite_remove), Toast.LENGTH_SHORT).show();
                } else {
                    fav.put(DatabaseHelper.KEY_ID, Id);
                    fav.put(DatabaseHelper.KEY_TITLE, objBean.getChannelName());
                    fav.put(DatabaseHelper.KEY_IMAGE, objBean.getImage());
                    fav.put(DatabaseHelper.KEY_CATEGORY, objBean.getChannelCategory());
                    databaseHelper.addFavourite(DatabaseHelper.TABLE_FAVOURITE_NAME, fav, null);
                    menu.findItem(R.id.menu_favourite).setIcon(R.drawable.favorite_hover);
                    Toast.makeText(ChannelDetailsActivity.this, getString(R.string.favourite_add), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void showRating() {
        final Dialog mDialog = new Dialog(ChannelDetailsActivity.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.rate_dialog);
        final RatingView ratingView = mDialog.findViewById(R.id.ratingView);
        ratingView.setRating(Float.parseFloat(objBean.getChannelAvgRate()));
        Button button = mDialog.findViewById(R.id.btn_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isConnected(ChannelDetailsActivity.this)) {
                    sentRate(String.valueOf(ratingView.getRating()));
                } else {
                    showToast(getString(R.string.conne_msg1));
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    public void sentRate(String rate) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("post_item_rate", "xxx");
        params.put("post_id", Id);
        params.put("user_id", myApp.getUserId());
        params.put("rate", rate);

        client.post(Constant.API_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dismissProgressDialog();
                String result = new String(responseBody);
                String rateMsg = "", newRate = "";
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        rateMsg = objJson.getString(Constant.MSG);
                        if (objJson.has(Constant.CHANNEL_AVG_RATE)) {
                            newRate = objJson.getString(Constant.CHANNEL_AVG_RATE);
                        }
                    }
                    showToast(rateMsg);
                    if (!newRate.isEmpty()) {
                        ratingView.setRating(Float.parseFloat(newRate));
                        textRate.setText(newRate);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
            }

        });
    }

    public void showToast(String msg) {
        Toast.makeText(ChannelDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (isFromNotification) {
            Intent intent = new Intent(ChannelDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }

    }

    private void isFavourite() {
        if (databaseHelper.getFavouriteById(Id)) {
            menu.findItem(R.id.menu_favourite).setIcon(R.drawable.favorite_hover);
        } else {
            menu.findItem(R.id.menu_favourite).setIcon(R.drawable.favorite_normal);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View v1 = findViewById(R.id.view_fake);
        v1.requestFocus();
    }

    private void playViaCast() {
        if (objBean.isTv()) {
            casty.getPlayer().loadMediaAndPlay(createSampleMediaData(objBean.getChannelUrl(), objBean.getChannelName(), objBean.getImage()));
        } else {
            showToast(getResources().getString(R.string.cast_youtube));
        }
    }

    private MediaData createSampleMediaData(String videoUrl, String videoTitle, String videoImage) {
        return new MediaData.Builder(videoUrl)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                .setContentType(getType(videoUrl))
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(videoTitle)
                .setSubtitle(getString(R.string.app_name))
                .addPhotoUrl(videoImage)
                .build();
    }

    private String getType(String videoUrl) {
        if (videoUrl.endsWith(".mp4")) {
            return "videos/mp4";
        } else if (videoUrl.endsWith(".m3u8")) {
            return "application/x-mpegurl";
        } else {
            return "application/x-mpegurl";
        }
    }


    private void showExternalPlay() {
        final Dialog mDialog = new Dialog(ChannelDetailsActivity.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.player_dialog);
        LinearLayout lytMxPlayer = mDialog.findViewById(R.id.lytMxPlayer);
        LinearLayout lytVLCPlayer = mDialog.findViewById(R.id.lytVLCPlayer);
        LinearLayout lytXMTVPlayer = mDialog.findViewById(R.id.lytXMTVPlayer);

        lytMxPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (isExternalPlayerAvailable(ChannelDetailsActivity.this, "com.mxtech.videoplayer.ad")) {
                    playMxPlayer();
                } else {
                    appNotInstalledDownload(getString(R.string.mx_player), "com.mxtech.videoplayer.ad", false);
                }
            }
        });

        lytVLCPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (isExternalPlayerAvailable(ChannelDetailsActivity.this, "org.videolan.vlc")) {
                    playVlcPlayer();
                } else {
                    appNotInstalledDownload(getString(R.string.vlc_player), "org.videolan.vlc", false);
                }
            }
        });

        lytXMTVPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (isExternalPlayerAvailable(ChannelDetailsActivity.this, "com.xmtex.videoplayer.ads")) {
                    playXmtvPlayer();
                } else {
                    appNotInstalledDownload(getString(R.string.xmtv_player), "com.xmtex.videoplayer.ads", true);
                }
            }
        });


        mDialog.show();
    }

    private void playMxPlayer() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri videoUri = Uri.parse(objBean.getChannelUrl());
        intent.setDataAndType(videoUri, "application/x-mpegURL");
        intent.setPackage("com.mxtech.videoplayer.ad");
        startActivity(intent);
    }

    private void playVlcPlayer() {
        Uri uri = Uri.parse(objBean.getChannelUrl());
        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
        vlcIntent.setPackage("org.videolan.vlc");
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
        vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
        startActivity(vlcIntent);
    }

    private void playXmtvPlayer() {
        Bundle bnd = new Bundle();
        bnd.putString("path", objBean.getChannelUrl());
        Intent intent = new Intent();
        intent.setClassName("com.xmtex.videoplayer.ads", "org.zeipel.videoplayer.XMTVPlayer");
        intent.putExtras(bnd);
        startActivity(intent);
    }

    private boolean isExternalPlayerAvailable(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void appNotInstalledDownload(final String appName, final String packageName, final boolean isDownloadExternal) {
        String text = getString(R.string.download_msg, appName);
        new AlertDialog.Builder(ChannelDetailsActivity.this)
                .setTitle(getString(R.string.important))
                .setMessage(text)
                .setPositiveButton(getString(R.string.download), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (isDownloadExternal) {
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getString(R.string.xmtv_download_link))));
                        } else {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id="
                                                + packageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id="
                                                + packageName)));
                            }
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }
}
