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


    private View rootLayout;
    private XWalkView mainWebView1;

    private CoreHttpServer coreHttpServer;
    private LinkedBlockingQueue<String> taskNoQueue = new LinkedBlockingQueue<String>();
    private ConcurrentHashMap<String, Map<String, String>> taskDetailHashMap = new ConcurrentHashMap<String, Map<String, String>>();


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
