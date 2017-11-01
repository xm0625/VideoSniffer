package com.xm.videosniffer;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xm.videosniffer.server.CoreHttpServer;

import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends Activity {

    private XWalkView mainWebView1;
    private XWalkView mainWebView2;
    private XWalkView mainWebView3;
    private XWalkView mainWebView4;

    private CoreHttpServer coreHttpServer;
    private LinkedBlockingQueue<String> taskNoQueue = new LinkedBlockingQueue<String>();
    private ConcurrentHashMap<String, Map<String, Object>> taskDetailHashMap = new ConcurrentHashMap<String, Map<String, Object>>();

    private WebWorker webWorker1;
    private WebWorker webWorker2;
    private WebWorker webWorker3;
    private WebWorker webWorker4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mainInit();
    }

    private void initView() {
        //TODO WebView按需创建和删除
        mainWebView1 = findViewById(R.id.mainWebView1);
        mainWebView2 = findViewById(R.id.mainWebView2);
        mainWebView3 = findViewById(R.id.mainWebView3);
        mainWebView4 = findViewById(R.id.mainWebView4);

        webWorker1 = new WebWorker(mainWebView1, taskNoQueue, taskDetailHashMap);
        webWorker2 = new WebWorker(mainWebView2, taskNoQueue, taskDetailHashMap);
        webWorker3 = new WebWorker(mainWebView3, taskNoQueue, taskDetailHashMap);
        webWorker4 = new WebWorker(mainWebView4, taskNoQueue, taskDetailHashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webWorker1.start();
        webWorker2.start();
        webWorker3.start();
        webWorker4.start();
    }

    @Override
    protected void onPause() {
        webWorker1.stop();
        webWorker2.stop();
        webWorker3.stop();
        webWorker4.stop();
        super.onPause();
    }

    private void mainInit() {
        coreHttpServer = new CoreHttpServer("0.0.0.0", 8080, taskNoQueue, taskDetailHashMap);
        try {
            coreHttpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
