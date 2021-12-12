package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter adapter;

    private List<String> namelist = new ArrayList<>();//歌曲名称数组
    private List<String> artistlist = new ArrayList<>();//艺术家名称数组
    private List<String> urllist = new ArrayList<>();//路径数组
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //5首歌歌名
        namelist.add("Little Grass Shack");
        namelist.add("Jingle Bells");
        namelist.add("Into the Unknown");
        namelist.add("Buckbreak");
        namelist.add("Oh Radiant One");
        //5个艺术家名
        artistlist.add("Voodoo Suite");
        artistlist.add("Scott Holmes");
        artistlist.add("Marrten");
        artistlist.add("Ken Hamm");
        artistlist.add("Siddhartha");
        //歌曲对应的url
        String url1="https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/Voodoo_Suite/blissbloodcom/Voodoo_Suite_-_03_-_Little_Grass_Shack.mp3?download=1&name=Voodoo%20Suite%20-%20Little%20Grass%20Shack.mp3";
        String url2="https://files.freemusicarchive.org/storage-freemusicarchive-org/tracks/99BX9qioxvFxilJgANMmPYcuG5I3IxTGchSFa1v8.mp3?download=1&name=Scott%20Holmes%20Music%20-%20Jingle%20Bells.mp3";
        String url3="https://files.freemusicarchive.org/storage-freemusicarchive-org/tracks/JuG1OD1wm6f3XD93haM4qSlwAfyBS4zrOcXstBPb.mp3?download=1&name=Maarten%20Schellekens%20-%20Into%20the%20Unknown.mp3";
        String url4="https://files.freemusicarchive.org/storage-freemusicarchive-org/music/Peppermill_Records/Ken_Hamm/Hi_and_Ho_We_Plant_Trees/Ken_Hamm_-_08_-_Buckbreak.mp3?download=1&name=Ken%20Hamm%20-%20Buckbreak.mp3";
        String url5="https://files.freemusicarchive.org/storage-freemusicarchive-org/tracks/mV3CPsPAboQ1mA4Ji0P2OiKeBNCsy3nPzJGys9l8.mp3?download=1&name=Siddhartha%20Corsus%20-%20Oh%20Radiant%20One.mp3";
        //添加url到urllist
        urllist.add(url1);
        urllist.add(url2);
        urllist.add(url3);
        urllist.add(url4);
        urllist.add(url5);
        adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,namelist);//数组适配器，显示歌曲名
        listView = findViewById(R.id.listview);
        listView.setAdapter(adapter);//给listview设置适配器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = namelist.get(position);
                Intent intent = new Intent(MainActivity.this,MusicPlayer.class);
                //因为intent只能不能传List，只能传String，因此需要转换
                String[] nameList={"","","","",""};
                for(int i=0;i<5;i++){
                    nameList[i]=namelist.get(i);
                }
                String[] artistList={"","","","",""};
                for(int i=0;i<5;i++){
                    artistList[i]=artistlist.get(i);
                }
                String[] urlStr = {"","","","",""};
                for(int i=0;i<5;i++){
                    urlStr[i]=urllist.get(i);
                }
                //通过intent把歌曲id、歌曲名、艺术家名和路径传给MusicPlayer
                intent.putExtra("position",position);
                intent.putExtra("artistList",artistList);
                intent.putExtra("nameList",nameList);
                intent.putExtra("urlStr",urlStr);
                startActivity(intent);//开启活动，音乐播放
            }
        });


    }
}