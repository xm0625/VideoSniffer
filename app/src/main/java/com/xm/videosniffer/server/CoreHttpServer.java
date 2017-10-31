package com.xm.videosniffer.server;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import fi.iki.elonen.NanoHTTPD;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xm on 2017/10/30.
 */

public class CoreHttpServer extends NanoHTTPD{
    private LinkedBlockingQueue<String> taskNoQueue;
    private ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap;


    public CoreHttpServer(String hostname, int port, LinkedBlockingQueue<String> taskNoQueue, ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap) {
        super(hostname, port);
        this.taskNoQueue = taskNoQueue;
        this.taskDetailHashMap = taskDetailHashMap;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, List<String>> parameters = session.getParameters();
        Log.d("CoreHttpServer", "uri:"+uri);
        Log.d("CoreHttpServer", "parameters:"+ JSONObject.toJSONString(parameters));
        HashMap<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("test", "2017");
        return respond(session, makeResponse("0", "ok", responseMap));
    }

    private String makeResponse(String code, String message, Object data){
        HashMap<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("code", code);
        responseMap.put("message", message);
        responseMap.put("data", data);
        return JSONObject.toJSONString(responseMap);
    }

    private Response respond(IHTTPSession session, String responseString) {
        // First let's handle CORS OPTION query
        Response response;
        if (Method.OPTIONS.equals(session.getMethod())) {
            response = new NanoHTTPD.Response(Response.Status.OK, MIME_PLAINTEXT, null, 0);
        } else {
            response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, responseString);
        }

        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,OPTIONS");
        response.addHeader("Access-Control-Max-Age", "" + (42 * 60 * 60));
        return response;
    }

    public ConcurrentHashMap<String, Map<String, String>> getTaskDetailHashMap() {
        return taskDetailHashMap;
    }

    public void setTaskDetailHashMap(ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap) {
        this.taskDetailHashMap = taskDetailHashMap;
    }

    public LinkedBlockingQueue<String> getTaskNoQueue() {
        return taskNoQueue;
    }

    public void setTaskNoQueue(LinkedBlockingQueue<String> taskNoQueue) {
        this.taskNoQueue = taskNoQueue;
    }
}
