package com.app.tvio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.util.Constant;
import com.app.util.IsRTL;
import com.app.util.NetworkUtils;
import com.github.ornolfr.ratingview.RatingView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ReportChannelActivity extends AppCompatActivity {
    ImageView imageChannel;
    TextView textName, textCategory,textMenu;
    RatingView ratingView;
    EditText editText;
    Button btnSubmit;
    String Id, cName, cCategory, cImage, cRate;
    ProgressDialog pDialog;
    MyApplication myApp;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_channel);
        IsRTL.ifSupported(ReportChannelActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.report_channel));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        myApp = MyApplication.getInstance();
        pDialog = new ProgressDialog(this);
        imageChannel = findViewById(R.id.image);
        textName = findViewById(R.id.text);
        textCategory = findViewById(R.id.textCategory);
        textMenu = findViewById(R.id.textViewOptions);
        ratingView = findViewById(R.id.ratingView);
        editText = findViewById(R.id.editIssue);
        btnSubmit = findViewById(R.id.btn_submit);

        textMenu.setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        Id = intent.getStringExtra("Id");
        cName = intent.getStringExtra("cName");
        cCategory = intent.getStringExtra("cCategory");
        cImage = intent.getStringExtra("cImage");
        cRate = intent.getStringExtra("cRate");

        Picasso.get().load(cImage).into(imageChannel);
        textName.setText(cName);
        textCategory.setText(cCategory);
        ratingView.setRating(Float.parseFloat(cRate));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String issue = editText.getText().toString();
                if (!issue.isEmpty()) {
                    if (NetworkUtils.isConnected(ReportChannelActivity.this)) {
                        sentIssue(issue);
                    } else {
                        Toast.makeText(ReportChannelActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void sentIssue(String issue) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("post_channel_report", "xxx");
        params.put("channel_id", Id);
        params.put("user_id", myApp.getUserId());
        params.put("report", issue);

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
                String rateMsg = "";
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    rateMsg = objJson.getString(Constant.MSG);
                    Toast.makeText(ReportChannelActivity.this, rateMsg, Toast.LENGTH_SHORT).show();
                    editText.setText("");
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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}
