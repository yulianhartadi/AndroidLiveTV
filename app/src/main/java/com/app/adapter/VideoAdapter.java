package com.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.tvio.R;
import com.app.tvio.VideoPlayActivity;
import com.app.tvio.YtPlayActivity;
import com.app.item.ItemVideo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by laxmi.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ItemRowHolder> {

    private ArrayList<ItemVideo> dataList;
    private Context mContext;

    public VideoAdapter(Context context, ArrayList<ItemVideo> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {
        final ItemVideo singleItem = dataList.get(position);
        holder.text.setText(singleItem.getVideoTitle());
        Picasso.get().load(singleItem.getVideoThumbnailB()).placeholder(R.drawable.placeholder).into(holder.image);
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleItem.getVideoType().equals("youtube")) {
                    Intent intent = new Intent(mContext, YtPlayActivity.class);
                    intent.putExtra("id", singleItem.getVideoId());
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, VideoPlayActivity.class);
                    intent.putExtra("videoUrl", singleItem.getVideoUrl());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView text;
        LinearLayout lyt_parent;

        ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            lyt_parent = itemView.findViewById(R.id.rootLayout);

        }
    }
}
