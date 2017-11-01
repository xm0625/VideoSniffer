package com.xm.videosniffer.event;

/**
 * Created by xm on 17-11-1.
 */
public class AutoPlayVideoEvent {
    private String workerNo;

    public AutoPlayVideoEvent(String workerNo) {
        this.workerNo = workerNo;
    }

    public String getWorkerNo() {
        return workerNo;
    }

    public void setWorkerNo(String workerNo) {
        this.workerNo = workerNo;
    }
}
