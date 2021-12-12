package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;

//自定义继承自Service的DownloadService子类，实现下载服务
public class DownloadService extends Service {

    private DownloadTask downloadTask;

    private String downloadUrl;

    //匿名内部类实例，实现接口的方法
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            //调用getNotification()方法构建一个用于显示下载进度的通知，调用notify()方法去触发通知，可以在下拉状态栏中实时看到下载进度
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            // 下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf(";")+1);
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            // 下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
        }

    };

    //创建一个Binder实例，在onBind方法中返回
    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("TAG","--DownLoadService--onBind()--");
        return mBinder;
    }

    //DownloadBinder类，与活动实现通信
    class DownloadBinder extends Binder {

        public void startDownload(String url) {
            Log.e("TAG","Downloading......");
            if (downloadTask == null) {

                downloadUrl = url;
                //创建呢Download的实例，将DownloadListener参数传入
                downloadTask = new DownloadTask(listener);
                //调用execute方法开启下载
                downloadTask.execute(downloadUrl);
                Notification notification=getNotification("Downloading...", 0);
                getNotificationManager().notify(1, notification);
                Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    // 取消下载时需将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf(";")+1);
                    File file = new File( fileName);
                    //file.delete();
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title,int progress)
    {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent ,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("AppTestNotificationId", "AppTestNotificationName", NotificationManager.IMPORTANCE_DEFAULT);
            getNotificationManager().createNotificationChannel(notificationChannel);
            builder.setChannelId("AppTestNotificationId");
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(bitmap);
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress >= 0)
        {
            builder.setContentText(progress + "%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

}
