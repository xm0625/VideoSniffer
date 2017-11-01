package com.xm.videosniffer.server;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.xm.videosniffer.util.UUIDUtil;
import fi.iki.elonen.NanoHTTPD;


import java.io.IOException;
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
    private ConcurrentHashMap<String, RequestHandler> routeMap = new ConcurrentHashMap<String, RequestHandler>();

    private LinkedBlockingQueue<String> taskNoQueue;
    private ConcurrentHashMap<String, Map<String, Object>> taskDetailHashMap;


    public CoreHttpServer(String hostname, int port, final LinkedBlockingQueue<String> taskNoQueue, final ConcurrentHashMap<String, Map<String, Object>> taskDetailHashMap) {
        super(hostname, port);
        this.taskNoQueue = taskNoQueue;
        this.taskDetailHashMap = taskDetailHashMap;

        routeMap.put("/task/add", new RequestHandler() {
            @Override
            public Object server(IHTTPSession session, Method method, String url, Map<String, List<String>> parameters) {
                if(!parameters.containsKey("url")){
                    throw new CommonException("param error");
                }
                String taskNo = UUIDUtil.genUUID();
                Map<String, Object> taskInfoMap = new HashMap<String, Object>();
                taskInfoMap.put("taskNo", taskNo);
                taskInfoMap.put("originalUrl", parameters.get("url").get(0));
                taskInfoMap.put("title", "");
                taskInfoMap.put("status", "queue");
                taskInfoMap.put("resultList", null);
                taskDetailHashMap.put(taskNo, taskInfoMap);
                taskNoQueue.add(taskNo);

                Map<String, String> resultMap = new HashMap<String, String>();
                resultMap.put("taskNo", taskNo);
                return resultMap;
            }
        });

        routeMap.put("/task/detail", new RequestHandler() {
            @Override
            public Object server(IHTTPSession session, Method method, String url, Map<String, List<String>> parameters) {
                if(!parameters.containsKey("taskNo")){
                    throw new CommonException("param error");
                }
                return taskDetailHashMap.get(parameters.get("taskNo").get(0));
            }
        });
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
            return respond(session, makeResponse("500", "IOException", null));
        } catch (ResponseException e) {
            e.printStackTrace();
            return respond(session, makeResponse("501", "ResponseException", null));
        }
        String uri = session.getUri();
        Map<String, List<String>> parameters = session.getParameters();
        Method method = session.getMethod();
        Log.d("CoreHttpServer", "uri:"+uri);
        Log.d("CoreHttpServer", "parameters:"+ JSONObject.toJSONString(parameters));
        Log.d("CoreHttpServer", "method:"+ method.toString());
        if(!routeMap.containsKey(uri)){
            return respond(session, makeResponse("404", "not found", null));
        }else{
            try{
                Object result = routeMap.get(uri).server(session, method, uri, parameters);
                return respond(session, makeResponse("0", "ok", result));
            }catch (CommonException e){
                return respond(session, makeResponse(e.getCode(), e.getMessage(), null));
            }
        }
    }

    class CommonException extends RuntimeException{
        private String code = "-1";
        private String message = "system busy";


        public CommonException(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public CommonException(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
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

    public ConcurrentHashMap<String, Map<String, Object>> getTaskDetailHashMap() {
        return taskDetailHashMap;
    }

    public void setTaskDetailHashMap(ConcurrentHashMap<String, Map<String, Object>> taskDetailHashMap) {
        this.taskDetailHashMap = taskDetailHashMap;
    }

    public LinkedBlockingQueue<String> getTaskNoQueue() {
        return taskNoQueue;
    }

    public void setTaskNoQueue(LinkedBlockingQueue<String> taskNoQueue) {
        this.taskNoQueue = taskNoQueue;
    }
}
