package com.example.musicplayer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    //下载状态
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;


    private DownloadListener listener;

    private boolean isCanceled = false;

    private boolean isPaused = false;

    private int lastProgress;

    //DownloadTask的构造函数传入DownloadListener参数，下载状态通过该参数回调
    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }
    @Override public void onPreExecute(){
        super.onPreExecute();
    }
    //后台执行具体的下载逻辑
    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0; //记录已下载的文件长度
            String downloadUrl = params[0];
            //解析出下载的文件名
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf(";")+1);
            String directory="";

            Log.e("TAG",directory+fileName);
            //获得下载url地址
            downloadUrl=downloadUrl.substring(0,downloadUrl.lastIndexOf(";"));
            file = new File(directory + fileName);
            if (file.exists()) //如果文件存在
            {
                downloadedLength = file.length();//获取文件已下载的长度
            }
            long contentLength = getContentLength(downloadUrl);//获得待下载文件的字节
            if (contentLength == 0) {//文件字节为0，失败
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                // 已下载字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }
            //发送网络请求
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    // 断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {//服务器响应
                //使用Java文件流方式不断从网上读取数据写入本地
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength); // 跳过已下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {//用户取消
                        return TYPE_CANCELED;
                    } else if(isPaused) {//用户暂停
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        // 计算已下载的百分比
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }
    //更新当前下载进度
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];//获得当前下载进度
        if (progress > lastProgress) {//和上一次下载进度对比
            listener.onProgress(progress);//调用DownloadListener的onProgress方法来通知下载进度更新
            lastProgress = progress;
        }
    }
    //通知最终的下载结果
    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {//根据下载状态来回调DownloadListener中的方法
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }


    public void cancelDownload() {
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

}