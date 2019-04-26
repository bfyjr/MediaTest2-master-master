package com.example.mediatest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class VideoUtil {



    public static List<VideoItem> getLocalVideos(Context context)
    {
        List<VideoItem> videoList=new ArrayList<>();

        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID};
        // 视频时常
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION};

        Cursor cursor=context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);
        if (cursor == null) {
            return videoList;
        }
        if (cursor.moveToFirst()) {
            do {
                VideoItem videoItem = new VideoItem();
                String path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media
                        .DATA));
                videoItem.setPath(path);
                videoItem.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video
                        .Media.DURATION)));
                videoList.add(videoItem);
            } while (cursor.moveToNext());
        }
        return videoList;
    }

    public static String splitName(String path)
    {
        String str[]=path.split("/");
        return str[str.length-1];
    }

    public static String formatTime(long time)
    {
        int second=(int)(time/1000);
        int minute=second/60;
        second=second%60;

        return twoChar(twoChar(minute+"")+":"+twoChar(second+""));
    }

    public static String twoChar(String s)
    {
        if(s.length()==1)
            return 0+""+s;
        return s;
    }

    public static Bitmap getVideoThumbnail(String videoPath) {
        MediaMetadataRetriever media =new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();
        return bitmap;
    }
    public static Bitmap getVideoThumbnail(String videoPath,Context context) {
        MediaMetadataRetriever media =new MediaMetadataRetriever();
        try
        {
            media.setDataSource(videoPath);
        }catch (Exception e)
        {
            Toast.makeText(context, "无法播放该视频", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(context,Activity2.class);
            context.startActivity(intent);
        }
        Bitmap bitmap = media.getFrameAtTime();
        return bitmap;
    }
}
