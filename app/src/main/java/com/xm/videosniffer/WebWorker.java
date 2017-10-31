package com.xm.videosniffer;

import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.xm.videosniffer.entity.DetectedVideoInfo;
import com.xm.videosniffer.entity.VideoInfo;
import com.xm.videosniffer.event.LoadEmptyPageEvent;
import com.xm.videosniffer.event.LoadTargetUrlPageEvent;
import com.xm.videosniffer.util.VideoFormatUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xwalk.core.*;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xm on 17-10-31.
 */
public class WebWorker {
    private static final String IPHONE_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
    private static final int TASK_TIMEOUT = 10000; // ms

    private WeakReference<XWalkView> weakReferenceXWalkView;
    private LinkedBlockingQueue<String> taskNoQueue;
    private ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap;

    private LinkedBlockingQueue<DetectedVideoInfo> detectedTaskUrlQueue = new LinkedBlockingQueue<DetectedVideoInfo>();
    private SortedMap<String, VideoInfo> foundVideoInfoMap = Collections.synchronizedSortedMap(new TreeMap<String, VideoInfo>());
    private String currentTitle = "";
    private String currentUrl = "";
    private VideoSniffer videoSniffer;

    private WorkThread workThread;

    public WebWorker(XWalkView xWalkView, LinkedBlockingQueue<String> taskNoQueue, ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap) {
        this.weakReferenceXWalkView = new WeakReference<XWalkView>(xWalkView);
        this.taskNoQueue = taskNoQueue;
        this.taskDetailHashMap = taskDetailHashMap;

        videoSniffer = new VideoSniffer(detectedTaskUrlQueue, foundVideoInfoMap, 5, 3);
        initWebView();
    }

    public void start(){
        stop();
        EventBus.getDefault().register(this);
        videoSniffer.startSniffer();
        workThread = new WorkThread();
        try {
            workThread.start();
        }catch (IllegalThreadStateException e){
            Log.d("VideoSniffer", "线程已启动, Pass");
        }
    }

    public void stop(){
        try {
            workThread.interrupt();
        }catch (Exception e){
            Log.d("VideoSniffer", "线程已中止, Pass");
        }
        videoSniffer.stopSniffer();
        EventBus.getDefault().unregister(this);
    }

    private class WorkThread extends Thread{
        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    String taskNo = taskNoQueue.take();
                    Log.d("WorkerThread", "start taskNo=" + taskNo);
                    Map<String, String> taskInfo = taskDetailHashMap.get(taskNo);
                    if(taskInfo == null){
                        continue;
                    }
                    //记录开始时间
                    long lastOperationStartTime = System.currentTimeMillis();
                    String originalUrl = taskInfo.get("originalUrl");
                    taskInfo.put("status", "loading");
                    EventBus.getDefault().post(new LoadTargetUrlPageEvent(originalUrl));
                    while((System.currentTimeMillis()- lastOperationStartTime)<TASK_TIMEOUT){
                        taskInfo.put("resultList", JSONArray.toJSONString(foundVideoInfoMap.values()));
                        Thread.sleep(500);
                    }
                    taskInfo.put("status", "done");
                    clearCurrentEnv();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d("WorkerThread", "thread (" + Thread.currentThread().getId() +") :Interrupted");
                    return;
                }
            }
        }
    }


    private void clearCurrentEnv() throws InterruptedException {
        EventBus.getDefault().post(new LoadEmptyPageEvent());
        Thread.sleep(500);
        detectedTaskUrlQueue.clear();
        foundVideoInfoMap.clear();
        currentTitle = "";
        currentUrl = "";
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadEmptyPageEvent(LoadEmptyPageEvent loadEmptyPageEvent){
        XWalkView xWalkView = weakReferenceXWalkView.get();
        if(xWalkView==null){
            return;
        }
        xWalkView.loadData("","text/html", "utf-8");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadTargetUrlPageEvent(LoadTargetUrlPageEvent loadTargetUrlPageEvent){
        XWalkView xWalkView = weakReferenceXWalkView.get();
        if(xWalkView==null){
            return;
        }
        xWalkView.loadUrl(loadTargetUrlPageEvent.getUrl());
    }

    private void initWebView() {
        //开启调式,支持谷歌浏览器调式
        XWalkView xWalkView = weakReferenceXWalkView.get();
        if(xWalkView==null){
            return;
        }
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        XWalkSettings webSettings = xWalkView.getSettings();
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(IPHONE_UA);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        xWalkView.requestFocus();

        xWalkView.setResourceClient(new MainXWalkResourceClient(xWalkView));
        xWalkView.setUIClient(new MainXWalkUIClient(xWalkView));
        XWalkCookieManager xm = new XWalkCookieManager();
        xm.setAcceptCookie(true);
    }

    private class MainXWalkUIClient extends XWalkUIClient {

        public MainXWalkUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onReceivedTitle(XWalkView view, String title) {
            super.onReceivedTitle(view, title);
            Log.d("MainActivity", "onReceivedTitle title=" + title);
            currentTitle = title;
        }

        @Override
        public void onPageLoadStarted(XWalkView view, String url) {
            super.onPageLoadStarted(view, url);
            Log.d("MainActivity", "onPageLoadStarted url=" + url);
            currentUrl = url;
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);
            Log.d("MainActivity", "onPageLoadStopped url=" + url);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
        }
    }

    private class MainXWalkResourceClient extends XWalkResourceClient{

        public MainXWalkResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onDocumentLoadedInFrame(XWalkView view, long frameId) {
            super.onDocumentLoadedInFrame(view, frameId);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
            Log.d("MainActivity", "onLoadStarted url:" + url);

            WeakReference<LinkedBlockingQueue> detectedTaskUrlQueueWeakReference = new WeakReference<LinkedBlockingQueue>(detectedTaskUrlQueue);
            Log.d("MainActivity", "shouldInterceptLoadRequest hint url:" + url);
            LinkedBlockingQueue  detectedTaskUrlQueue = detectedTaskUrlQueueWeakReference.get();
            if(detectedTaskUrlQueue != null){
                detectedTaskUrlQueue.add(new DetectedVideoInfo(url,currentUrl,currentTitle));
                Log.d("MainActivity", "shouldInterceptLoadRequest detectTaskUrlList.add(url):" + url);
            }
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);
        }

        @Override
        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
            Log.d("MainActivity", "onProgressChanged progressInPercent=" + progressInPercent);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
            XWalkWebResourceResponse xWalkWebResourceResponse = super.shouldInterceptLoadRequest(view, request);
            String url = request.getUrl().toString();
            Log.d("MainActivity", "shouldInterceptLoadRequest url:" + url);
            return xWalkWebResourceResponse;
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                //非http https协议 不动作
                return true;
            }

            //http https协议 在本webView中加载
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if(VideoFormatUtil.containsVideoExtension(extension)){
                detectedTaskUrlQueue.add(new DetectedVideoInfo(url,currentUrl,currentTitle));
                Log.d("MainActivity", "shouldOverrideUrlLoading detectTaskUrlList.add(url):" + url);
                return true;
            }

            Log.d("MainActivity", "shouldOverrideUrlLoading url="+url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
            Log.d("MainActivity", "onReceivedSslError");
        }
    }

}
