package com.app.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.app.adapter.CategoryAdapter;
import com.app.adapter.ChannelAdapter;
import com.app.adapter.SliderAdapter;
import com.app.tvio.MainActivity;
import com.app.tvio.R;
import com.app.item.ItemCategory;
import com.app.item.ItemChannel;
import com.app.util.Constant;
import com.app.util.ItemOffsetDecoration;
import com.app.util.NetworkUtils;
import com.app.util.RecyclerTouchListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import me.relex.circleindicator.CircleIndicator;

/**
 * Created by laxmi.
 */

public class HomeFragment extends Fragment {

    ArrayList<ItemChannel> mSliderList, mLatestList;
    ArrayList<ItemCategory> mCategoryList;
    SliderAdapter sliderAdapter;
    ScrollView mScrollView;
    ProgressBar mProgressBar;
    ViewPager mViewPager;
    CircleIndicator circleIndicator;
    RecyclerView rvLatest, rvCategory;
    Button btnMoreLatest, btnMoreCategory;
    ChannelAdapter channelAdapter;
    CategoryAdapter categoryAdapter;
    LinearLayout lyt_not_found;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mSliderList = new ArrayList<>();
        mLatestList = new ArrayList<>();
        mCategoryList = new ArrayList<>();
        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        mScrollView = rootView.findViewById(R.id.scrollView);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mViewPager = rootView.findViewById(R.id.viewPager);
        circleIndicator = rootView.findViewById(R.id.indicator_unselected_background);
        rvLatest = rootView.findViewById(R.id.rv_latest);
        rvCategory = rootView.findViewById(R.id.rv_category);
        btnMoreLatest = rootView.findViewById(R.id.btn_latest);
        btnMoreCategory = rootView.findViewById(R.id.btn_category);

        rvLatest.setHasFixedSize(true);
        rvLatest.setNestedScrollingEnabled(false);
        rvLatest.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        rvCategory.setHasFixedSize(true);
        rvCategory.setNestedScrollingEnabled(false);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(requireActivity(), R.dimen.item_offset);
        rvLatest.addItemDecoration(itemDecoration);
        rvCategory.addItemDecoration(itemDecoration);

        btnMoreLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).hideShowBottomView(false);
                ((MainActivity) requireActivity()).navigationItemSelected(1);
                String categoryName = getString(R.string.menu_latest);
                FragmentManager fm = getFragmentManager();
                LatestFragment f1 = new LatestFragment();
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Container, f1, categoryName);
                ft.commit();
                ((MainActivity) requireActivity()).setToolbarTitle(categoryName);
            }
        });

        btnMoreCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).hideShowBottomView(false);
                ((MainActivity) requireActivity()).navigationItemSelected(2);
                String categoryName = getString(R.string.menu_category);
                FragmentManager fm = getFragmentManager();
                CategoryFragment f1 = new CategoryFragment();
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Container, f1, categoryName);
                ft.commit();
                ((MainActivity) requireActivity()).setToolbarTitle(categoryName);
            }
        });

        rvCategory.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rvCategory, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ((MainActivity) requireActivity()).hideShowBottomView(false);
                ((MainActivity) requireActivity()).navigationItemSelected(2);
                String categoryName = mCategoryList.get(position).getCategoryName();
                Bundle bundle = new Bundle();
                bundle.putString("name", categoryName);
                bundle.putString("Id", mCategoryList.get(position).getCategoryId());

                FragmentManager fm = getFragmentManager();
                CategoryItemFragment categoryItemFragment = new CategoryItemFragment();
                categoryItemFragment.setArguments(bundle);
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(HomeFragment.this);
                ft.add(R.id.Container, categoryItemFragment, categoryName);
                ft.addToBackStack(categoryName);
                ft.commit();
                ((MainActivity) requireActivity()).setToolbarTitle(categoryName);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (NetworkUtils.isConnected(getActivity())) {
            getHome();
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }

    private void getHome() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constant.HOME_URL, new AsyncHttpResponseHandler() {

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
                    JSONObject jsonArray = mainJson.getJSONObject(Constant.ARRAY_NAME);

                    JSONArray jsonSlider = jsonArray.getJSONArray(Constant.HOME_SLIDER_ARRAY);
                    JSONObject objJson;
                    for (int i = 0; i < jsonSlider.length(); i++) {
                        objJson = jsonSlider.getJSONObject(i);
                        ItemChannel objItem = new ItemChannel();
                        objItem.setId(objJson.getString(Constant.CHANNEL_ID));
                        objItem.setChannelName(objJson.getString(Constant.CHANNEL_TITLE));
                        objItem.setChannelCategory(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setImage(objJson.getString(Constant.CHANNEL_IMAGE));
                        objItem.setChannelAvgRate(objJson.getString(Constant.CHANNEL_AVG_RATE));
                        mSliderList.add(objItem);
                    }

                    JSONArray jsonLatest = jsonArray.getJSONArray(Constant.HOME_LATEST_ARRAY);
                    for (int i = 0; i < jsonLatest.length(); i++) {
                        objJson = jsonLatest.getJSONObject(i);
                        ItemChannel objItem = new ItemChannel();
                        objItem.setId(objJson.getString(Constant.CHANNEL_ID));
                        objItem.setChannelName(objJson.getString(Constant.CHANNEL_TITLE));
                        objItem.setChannelCategory(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setImage(objJson.getString(Constant.CHANNEL_IMAGE));
                        objItem.setChannelAvgRate(objJson.getString(Constant.CHANNEL_AVG_RATE));
                        mLatestList.add(objItem);
                    }

                    JSONArray jsonCategory = jsonArray.getJSONArray(Constant.HOME_CATEGORY_ARRAY);
                    for (int i = 0; i < jsonCategory.length(); i++) {
                        objJson = jsonCategory.getJSONObject(i);
                        ItemCategory objItem = new ItemCategory();
                        objItem.setCategoryId(objJson.getString(Constant.CATEGORY_CID));
                        objItem.setCategoryName(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setCategoryImage(objJson.getString(Constant.CATEGORY_IMAGE));
                        mCategoryList.add(objItem);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
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
        sliderAdapter = new SliderAdapter(requireActivity(), mSliderList);
        mViewPager.setAdapter(sliderAdapter);
        circleIndicator.setViewPager(mViewPager);

        channelAdapter = new ChannelAdapter(getActivity(), mLatestList, R.layout.row_home_channel_item);
        rvLatest.setAdapter(channelAdapter);

        categoryAdapter = new CategoryAdapter(getActivity(), mCategoryList, R.layout.row_home_category_item);
        rvCategory.setAdapter(categoryAdapter);
    }

}
