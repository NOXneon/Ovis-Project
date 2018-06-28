package neon.ovis;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessaging extends FirebaseMessagingService {

    private TDB db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = new TDB(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData().size()>0)
        {
            if(remoteMessage.getData().get("message") != null)
            {
                String title = (remoteMessage.getData().get("title") != null) ? remoteMessage.getData().get("title") : "Notification";
                showNotification(title, remoteMessage.getData().get("message"));
            }
        }

        if(remoteMessage.getNotification() != null)
        {
            String title = (remoteMessage.getData().get("title") != null) ? remoteMessage.getData().get("title") : "Notification";
            showNotification(title, remoteMessage.getNotification().getBody());
        }

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        try {
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            String date = String.format("%02d",today.monthDay)+"/"+String.format("%02d",today.month+1)+"/"+today.year;
            String title = notification.getTitle();
            if(title.isEmpty())
                title = "Notification";
            Notification n = new Notification(title, notification.getBody(), date);
            db.insertNtf(n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    /*
    private void showNotification(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
    */
}
