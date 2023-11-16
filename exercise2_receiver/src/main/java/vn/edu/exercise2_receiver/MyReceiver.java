package vn.edu.exercise2_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Received", Toast.LENGTH_SHORT).show();
        String message = intent.getExtras().getString("message");
        NotificationHelper.showNotification(context, "New message received!", message);
    }
}