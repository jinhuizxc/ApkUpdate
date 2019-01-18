package demo.com.apkupdate.update;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载调度管理器，调用我们的UpdateDownLoadRequest
 */
public class UpdateManager {

    private static UpdateManager updateManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    private static final String TAG = "tag";

    private UpdateManager(){
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    /**
     * 单例模式
     */
    static{
        updateManager = new UpdateManager();
    }

    public static UpdateManager getInstance(){
        return updateManager;
    }

    /**
     * 开始下载方法
     * @param downloadUrl
     * @param localPath
     * @param listener
     */
    public void startDownloads(String downloadUrl, String localPath, UpdateDownloadListener listener){
        if(request != null){
            return;
        }
        checkLocalFilePath(localPath);
        //开始真正的下载任务
        request = new UpdateDownloadRequest(downloadUrl, localPath, listener);
        Future<?> future = threadPoolExecutor.submit(request);
    }

    /**
     * 用来检查文件路径是否已经存在
     * @param path
     */
    private void checkLocalFilePath(String path) {
        Log.e(TAG, path);
        File dir = new File(path.substring(0, path.lastIndexOf("/")+1));
        if(!dir.exists()){
            dir.mkdir();
        }
        File file = new File(path);
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
