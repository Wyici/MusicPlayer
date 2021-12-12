package com.example.musicplayer;

public interface DownloadListener//回调接口，用于对下载过程中各种状态进行监听和回调
{
    void onProgress(int progress);//用于通知当前的下载进度
    void onSuccess();//用于通知下载成功事件
    void onFailed();//用于通知下载失败事件
    void onPaused();//用于通知下载暂停事件
    void onCanceled();//用于通知下载取消事件
}
