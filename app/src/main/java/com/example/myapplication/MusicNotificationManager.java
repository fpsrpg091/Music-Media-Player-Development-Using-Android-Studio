package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;

public class MusicNotificationManager {

    public static final String CHANNEL_ID = "MusicServiceChannel";
    public static final int NOTIFICATION_ID = 1; // Changed to public
    private Context context;
    private NotificationManager notificationManager;
    public static final String ACTION_UPDATE_SONG_TITLE = "ACTION_UPDATE_SONG_TITLE"; // New action for broadcasting

    public MusicNotificationManager(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    public Notification createNotification() {
        MusicService musicService = (MusicService) context;

        Intent playIntent = new Intent(context, MusicService.class).setAction(MusicService.ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(context, MusicService.class).setAction(MusicService.ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(context, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(context, MusicService.class).setAction(MusicService.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(context, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(context, MusicService.class).setAction(MusicService.ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getService(context, 3, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int playPauseBtn = musicService.isPlaying() ? R.drawable.player_pause : R.drawable.player_play;

        Bitmap albumCover = BitmapFactory.decodeResource(context.getResources(), R.drawable.album);

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
        notificationLayout.setTextViewText(R.id.textViewTitle, musicService.getCurrentSongTitle());
        String currentSongTitle = musicService.getCurrentSongTitle(); // Get the current song title

        notificationLayout.setImageViewBitmap(R.id.imageViewAlbum, albumCover);
        notificationLayout.setImageViewResource(R.id.btnPrevious, R.drawable.player_back);
        notificationLayout.setImageViewResource(R.id.btnPlayPause, playPauseBtn);
        notificationLayout.setImageViewResource(R.id.btnNext, R.drawable.player_next);

        notificationLayout.setOnClickPendingIntent(R.id.btnPrevious, previousPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.btnPlayPause, musicService.isPlaying() ? pausePendingIntent : playPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.btnNext, nextPendingIntent);

        // **New Code: Broadcast the current song title**
        Intent broadcastIntent = new Intent(ACTION_UPDATE_SONG_TITLE);
        broadcastIntent.putExtra("CURRENT_SONG_TITLE", currentSongTitle);
        context.sendBroadcast(broadcastIntent);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Playing Music")
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(playPendingIntent)
                .build();
    }

    public void updateNotification() {
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
