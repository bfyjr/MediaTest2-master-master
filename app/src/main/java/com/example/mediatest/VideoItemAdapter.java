package com.example.mediatest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ViewHolder> {
    private Context context;
    private List<VideoItem> videoItemList;
    private boolean isQianTao=false;//区分首页还是播放页的适配器
    private OnItemClickListener onItemClickListener = null;
    private boolean queryFlag=false;
    private String queryStr="";

    //setter方法
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    //回调接口
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        ImageView cardImageView;
        TextView cardNameTextView;
        TextView cardDuraTextView;
        View wholeCardView;

        public ViewHolder(View view)
        {
            super(view);
            wholeCardView=view;
            cardView= (CardView) view;
            cardImageView=view.findViewById(R.id.card_imageview);
            cardNameTextView=view.findViewById(R.id.card_name_textview);
            cardDuraTextView=view.findViewById(R.id.card_duration_textview);
        }

    }
    public VideoItemAdapter(List<VideoItem> videoItemList1)
    {
        videoItemList=videoItemList1;
        queryFlag=false;
    }
    public VideoItemAdapter(List<VideoItem> videoItemList1,boolean queryFlag,String queryStr)
    {
        videoItemList=videoItemList1;
        this.queryFlag=queryFlag;
        this.queryStr=queryStr;
    }

    public VideoItemAdapter(List<VideoItem> videoItemList1,boolean isQianTao)
    {
        videoItemList=videoItemList1;
        this.isQianTao=isQianTao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(context==null)
            context = parent.getContext();
        View view=null;
        if(!isQianTao)
        {
            view = LayoutInflater.from(context).inflate(R.layout.video_cardview,parent,false);
        }else
        {
            view = LayoutInflater.from(context).inflate(R.layout.video_cardview,null,false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        VideoItem videoItem=videoItemList.get(position);
        holder.cardNameTextView.setText(VideoUtil.splitName(videoItem.getPath()));
        holder.cardDuraTextView.setText(VideoUtil.formatTime(videoItem.getDuration()));
        if(videoItem.getDuration()==0)
        {
            holder.cardDuraTextView.setText(VideoUtil.formatTime(videoItem.getDuration())+"（不能播放）");
        }
        if(queryFlag)
        {
            SpannableStringBuilder builder = new SpannableStringBuilder(holder.cardNameTextView.getText().toString());
            ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(redSpan, VideoUtil.splitName(videoItem.getPath()).indexOf(queryStr),
                    VideoUtil.splitName(videoItem.getPath()).indexOf(queryStr)+queryStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.cardNameTextView.setText(builder);
        }
        try
        {
            Glide.with(context).load(videoItem.getPath()).into(holder.cardImageView);
        }catch (Exception e)
        {}

        //实现点击效果
        holder.wholeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoItemList.size();
    }
}
