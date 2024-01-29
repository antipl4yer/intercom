package ru.samsung.smartintercom.service.notification;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import ru.samsung.smartintercom.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.NotificationManager;
import android.app.NotificationChannel;
import androidx.core.app.NotificationCompat;
import android.Manifest;

public class SystemNotificationService {
    private static final String CHANNEL_ID = "smart_intercom_channel";
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private final Context _context;

    public SystemNotificationService(Context context) {
        _context = context;

        createNotificationChannel();
    }

    public void send(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Random random = new Random();
        int notificationId = random.nextInt();

        NotificationManager notificationManager = _context.getSystemService(NotificationManager.class);
        notificationManager.notify(notificationId, builder.build());
    }

    public void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int notificationPermission = _context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS);

            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {

                List<String> listPermissionsNeeded = new ArrayList<>();
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);

                ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = _context.getString(R.string.notification_channel_name);
            String description = _context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = _context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
