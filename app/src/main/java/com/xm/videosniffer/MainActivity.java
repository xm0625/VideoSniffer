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


public class MainActivity extends Activity {

    private static final String IPHONE_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";

    private View rootLayout;
    private XWalkView mainWebView1;
    private XWalkView mainWebView2;
    private XWalkView mainWebView3;
    private XWalkView mainWebView4;

    private CoreHttpServer coreHttpServer;
    private ConcurrentLinkedQueue<String> taskNoQueueList;
    private ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mainInit();
    }

    private void initView() {
        rootLayout = findViewById(R.id.rootLayout);
        mainWebView1 = (XWalkView) findViewById(R.id.mainWebView1);
        mainWebView2 = (XWalkView) findViewById(R.id.mainWebView2);
        mainWebView3 = (XWalkView) findViewById(R.id.mainWebView3);
        mainWebView4 = (XWalkView) findViewById(R.id.mainWebView4);


    }

    private void mainInit() {
        coreHttpServer = new CoreHttpServer("0.0.0.0", 8080);
        try {
            coreHttpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        taskNoQueueList = coreHttpServer.getTaskNoQueueList();
        taskDetailHashMap = coreHttpServer.getTaskDetailHashMap();


    }

}
