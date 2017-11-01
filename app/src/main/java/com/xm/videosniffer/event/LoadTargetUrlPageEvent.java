package com.xm.videosniffer.event;

/**
 * Created by xm on 17-10-31.
 */
public class LoadTargetUrlPageEvent {
    private String workerNo;
    private String url;

    public LoadTargetUrlPageEvent(String workerNo, String url) {
        this.workerNo = workerNo;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWorkerNo() {
        return workerNo;
    }

    public void setWorkerNo(String workerNo) {
        this.workerNo = workerNo;
    }
}
