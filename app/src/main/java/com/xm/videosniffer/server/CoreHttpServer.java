package com.xm.videosniffer.server;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import fi.iki.elonen.NanoHTTPD;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by xm on 2017/10/30.
 */

public class CoreHttpServer extends NanoHTTPD{
    private ConcurrentLinkedQueue<String> taskNoQueueList = new ConcurrentLinkedQueue<String>();
    private ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap = new ConcurrentHashMap<String, Map<String, String>>();

    public CoreHttpServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, List<String>> parameters = session.getParameters();
        Log.d("CoreHttpServer", "uri:"+uri);
        Log.d("CoreHttpServer", "parameters:"+ JSONObject.toJSONString(parameters));
        return super.serve(session);
    }

    public ConcurrentLinkedQueue<String> getTaskNoQueueList() {
        return taskNoQueueList;
    }

    public void setTaskNoQueueList(ConcurrentLinkedQueue<String> taskNoQueueList) {
        this.taskNoQueueList = taskNoQueueList;
    }

    public ConcurrentHashMap<String, Map<String, String>> getTaskDetailHashMap() {
        return taskDetailHashMap;
    }

    public void setTaskDetailHashMap(ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap) {
        this.taskDetailHashMap = taskDetailHashMap;
    }
}
