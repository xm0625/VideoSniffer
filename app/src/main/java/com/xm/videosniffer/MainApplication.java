package com.xm.videosniffer;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xm on 17-8-16.
 */
public class MainApplication extends Application {

    public static MainApplication mainApplication = null;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        onAppInit();
    }

    private void onAppInit(){
        mainApplication = this;
    }

}
