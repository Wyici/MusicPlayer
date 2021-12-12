package com.example.musicplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MusicPlayer extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private static final int UPDATE = 1;
    private static final int MUSICDURATION = 0;
    private boolean hadPlay= false;
    private int curTime = 0;
    private int Duration = 0;
    private String musicname = "";
    private String artistname = "";
    private String url = "";
    private int position;
    private String[] nameList = {"","","","",""};
    private String[] artistList = {"","","","",""};
    private String[] urlStr = {"","","","",""};
    SeekBar seekBar;
    Button last,next,play,pause;
    TextView name,artist;
    TextView curtime,duration;
    private MediaPlayer mediaPlayer = new MediaPlayer();//MediaPlayer对象
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {//匿名内部类
        @Override
        //接收Binder，并使用提供的方法调用绑定服务
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("TAG","--downLoadBinder--");
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override

        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Handler handler=new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MUSICDURATION:
                    seekBar.setMax(mediaPlayer.getDuration());
                    break;
                case UPDATE:
                    try {
                        curTime = mediaPlayer.getCurrentPosition();
                        curtime.setText(formatTime(curTime));
                        seekBar.setProgress((int) (curTime*100.0/(Duration*1.0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessageDelayed(UPDATE,500);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        name = findViewById(R.id.name);
        artist = findViewById(R.id.artist);
        curtime = findViewById(R.id.curtime);//当前播放至
        curtime.setText(formatTime(curTime));
        duration = findViewById(R.id.duration);//歌曲总时长
        duration.setText(formatTime(Duration));
        //绑定seekbar监听
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        //绑定按钮
        last = findViewById(R.id.last);
        next = findViewById(R.id.next);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        last.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        Intent intent = getIntent();
        //通过intent获取mainactivity传递过来的信息，歌曲名称，url等
        position = intent.getIntExtra("position",-1);
        nameList = intent.getStringArrayExtra("nameList");
        artistList = intent.getStringArrayExtra("artistList");
        urlStr = intent.getStringArrayExtra("urlStr");
        musicname = nameList[position];
        artistname = artistList[position];
        url = urlStr[position];
        //在界面中设置歌曲名和艺术家
        name.setText(musicname);
        artist.setText(artistname);
        //申请权限
        if(ContextCompat.checkSelfPermission(MusicPlayer.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MusicPlayer.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else{
            initMediaPlayer();
        }
    }
    private void initMediaPlayer(){//初始化播放
        Log.e("TAG","--initMediaPlayer()--");
        //File file = new File(getFilesDir(), musicname + ".mp3");//查找是否有音乐对应的文件

        if(!hadPlay) {
            Intent intent = new Intent(MusicPlayer.this, DownloadService.class);
            startService(intent); // 启动服务
            bindService(intent, connection, BIND_AUTO_CREATE); // 绑定服务
            if (ContextCompat.checkSelfPermission(MusicPlayer.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MusicPlayer.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode,String[] permissions,
                                           int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else{
                    Toast.makeText(this,"拒绝权限将无法使用程序",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    @Override
    public void onClick(View v) {
        Log.e("TAG","--onClick()--");
        switch (v.getId()){
            case R.id.last://播放上一首
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position > 0)
                {
                    Intent intent = new Intent(MusicPlayer.this, MusicPlayer.class);
                    intent.putExtra("urlStr",urlStr);
                    intent.putExtra("position", position-1);
                    intent.putExtra("nameList", nameList);
                    intent.putExtra("artistList",artistList);
                    startActivity(intent);
                }else
                    Toast.makeText(MusicPlayer.this,"已经是第一首歌！",Toast.LENGTH_SHORT).show();
                break;
            case R.id.next:
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position < nameList.length - 1)
                {
                    Intent intent = new Intent(MusicPlayer.this, MusicPlayer.class);
                    intent.putExtra("position", position+1);
                    intent.putExtra("nameList", nameList);
                    intent.putExtra("urlStr",urlStr);
                    intent.putExtra("artistList",artistList);
                    startActivity(intent);
                }else
                    Toast.makeText(MusicPlayer.this,"已经是最后一首歌！",Toast.LENGTH_SHORT).show();
                break;
            case R.id.play:
                if(hadPlay == false)
                {
                    File file = new File(getFilesDir(), musicname + ".mp3");
                    String filePath=getFilesDir()+"/"+musicname + ".mp3";
                    if(!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (downloadBinder != null) {
                            downloadBinder.startDownload(url + ";" + filePath);
                        }
                    }
                    try{
                        mediaPlayer.setDataSource(file.getPath());
                        Log.e("TAG","--mediaPlayer.prepare()--");
                        mediaPlayer.prepare();
                        Duration = mediaPlayer.getDuration();
                        duration.setText(formatTime(Duration));
                        Log.e("TAG", "--initMediaPlayer()---END---");
                        hadPlay=true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        hadPlay=false;
                    }
                }
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    handler.sendEmptyMessage(UPDATE);  //发送Message
                }
                break;
            case R.id.pause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;
        }
    }
    //seekbar方法
    @Override
    //由seekbar进度知道现在播放到了哪里
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser == true)
        {
            curTime = (int) (progress * 1.0 / 100.0 * Duration);
        }
        curtime.setText(formatTime(curTime));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    //seekbar拖动
    public void onStopTrackingTouch(SeekBar seekBar) {
        curTime = (int) (seekBar.getProgress() * 1.0 / 100.0 * Duration);
        curtime.setText(formatTime(curTime));
        mediaPlayer.seekTo(curTime);
    }
    //时间格式化
    private String formatTime(int length)
    {
        Date date = new Date(length);
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        String totalTime = dateFormat.format(date);
        return totalTime;
    }
    protected void onDestroy() {
        Log.e("TAG","--onDestroy()--");
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        unbindService(connection);
    }
}