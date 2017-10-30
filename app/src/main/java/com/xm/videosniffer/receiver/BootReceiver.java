package com.xm.videosniffer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.xm.videosniffer.MainActivity;

import java.util.logging.Logger;

/**
 * Created by xm on 17-10-30.
 */
public class BootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver", "enter...");
        Intent intent1=new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
        Log.d("BootReceiver", "exit...");
    }
}
