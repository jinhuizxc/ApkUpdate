package demo.com.apkupdate.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 *真正的负责处理文件下载和线程间的通信
 */
public class UpdateDownloadRequest implements Runnable{

    private String downloadUrl;//文件下载路径
    private String localFilePath;//文件保存路径
    private UpdateDownloadListener listener;//接口回调
    private boolean isDownloading = false;//下载的标志位
    private long currentLength;//文件长度

    private DownloadResponseHandler downloadResponseHandler;
    private static final String TAG = "tag";

    public UpdateDownloadRequest(String downloadUrl, String localFilePath, UpdateDownloadListener listener){

        this.listener = listener;
        this.downloadUrl = downloadUrl;
        this.localFilePath = localFilePath;
        this.isDownloading = true;
        this.downloadResponseHandler = new DownloadResponseHandler();

    }

    /**
     * 真正的去建立连接的方法
     * @throws IOException
     * @throws InterruptedException
     */
    private void makeRequest() throws IOException, InterruptedException{

        if(!Thread.currentThread().isInterrupted()){

            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect();//阻塞我们当前的线程
                currentLength = connection.getContentLength();
                if(!Thread.currentThread().isInterrupted()){
                    //真正的完成文件的下载
                    downloadResponseHandler.sendResponseMessage(connection.getInputStream());
                }
            }catch (IOException e){
                throw e;
            }
        }
    }

    @Override
    public void run() {

        try{
            makeRequest();
        }catch (IOException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * 格式化数字
     * @param value
     * @return
     */
    private String getTwoPointFloatStr(float value){
        DecimalFormat df = new DecimalFormat("0.00000000000");
        return df.format(value);

    }

    /**
     * 包含了下载过程中所有可能出现的异常情况
     */
    public enum FailureCode{
        UnknownHost, Socket, SocketTimeout, connectionTimeout,IO, HttpResponse,
        Json, Interrupted

    }


    /**
     * 用来正在的去下载文件，并发送消息和回调的接口
     */
    public class DownloadResponseHandler{

        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINISH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        private static final int PROGRESS_CHANGED = 5;

        private float completeSize = 0;
        private int progress = 0;

        private Handler handler;//真正的完成线程间的通信

        public DownloadResponseHandler(){

            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);
                }
            };

        }

        /**
         * 用来发送不同的消息对象
         */

        protected void sendFinishMessage(){
            sendMessage(obtainMessage(FINISH_MESSAGE, null));
        }

        private void sendProgressChangedMessage(int progress){
            sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[]{progress}));

        }

        protected void sendFailureMessage(FailureCode failureCode){
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode}));

        }

        protected void sendMessage(Message msg){
            if(handler!=null){
                handler.sendMessage(msg);
            }else{
                handleSelfMessage(msg);
            }

        }

        /**
         * 获取一个消息对象
         * @param responseMessge
         * @param response
         * @return
         */
        protected Message obtainMessage(int responseMessge, Object response){
            Message msg = null;
            if(handler!=null){
                msg = handler.obtainMessage(responseMessge, response);
            }else{
                msg = Message.obtain();
                msg.what = responseMessge;
                msg.obj = response;
            }
            return msg;

        }

        protected void handleSelfMessage(Message msg){

            Object[] response;
            switch (msg.what){
                case FAILURE_MESSAGE:
                    response = (Object[]) msg.obj;
                    sendFailureMessage((FailureCode) response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response = (Object[]) msg.obj;
                    handleProgressChangedMessage(((Integer)response[0]).intValue());
                    break;
                case FINISH_MESSAGE:
                    onFinish();
                    break;
            }
        }

        /**
         * 各种消息的处理逻辑
         * @param progress
         */
        protected void handleProgressChangedMessage(int progress){
            listener.onProgressChanged(progress, downloadUrl);
        }

        /**
         * 外部接口的回调
         */
        protected void onFinish(){
            listener.onFinished(completeSize, "");

        }

        private void handleFailureMessage(FailureCode failureCode){
            onFailure(failureCode);

        }

        protected void onFailure(FailureCode failureCode){

            listener.onFailure();
        }

        /**
         * 文件下载方法，会发送各种类型的事件
         * @param is
         */
        void sendResponseMessage(InputStream is){

            RandomAccessFile randomAccessFile = null;//文件读写流
            completeSize=0;
            try{
                byte[] buffer = new byte[1024];
                int length=-1;//读写长度
                int limit=0;
                randomAccessFile = new RandomAccessFile(localFilePath, "rwd");//可读可写模式

                while((length = is.read(buffer))!=-1){

                    if(isDownloading){

                        randomAccessFile.write(buffer, 0 ,length);
                        completeSize += length;
                        if(completeSize < currentLength){
                            Log.e(TAG, "completeSize="+completeSize);
                            Log.e(TAG, "currentLength="+currentLength);
                            progress = (int)(Float.parseFloat(getTwoPointFloatStr(completeSize/currentLength))*100);
                            Log.e(TAG, "下载进度："+progress);
                            if(limit / 30==0 || progress <= 100){//为了限制一下我们notification的更新频率
                                sendProgressChangedMessage(progress);
                            }
                            limit++;
                        }
                    }
                }
                sendFinishMessage();
            }catch(IOException e){
                sendFailureMessage(FailureCode.IO);
            }finally{
                try{
                    if(is!=null){
                        is.close();
                    }
                    if(randomAccessFile!=null){
                        randomAccessFile.close();
                    }
                }catch(IOException e){
                    sendFailureMessage(FailureCode.IO);
                }
            }


        }
    }
}
