package com.xm.videosniffer.event;

/**
 * Created by xm on 17-10-31.
 */
public class LoadTargetUrlPageEvent {
    private String url;

    public LoadTargetUrlPageEvent(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
