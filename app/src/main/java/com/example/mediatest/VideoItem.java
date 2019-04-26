package com.example.mediatest;

import java.io.Serializable;

public class VideoItem implements Serializable {
    private String path;
    private long duration;
    public VideoItem()
    {
    }

    public VideoItem(String path,long duration)
    {
        this.path=path;
        this.duration=duration;

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
