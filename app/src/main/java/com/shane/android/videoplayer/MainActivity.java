package com.shane.android.videoplayer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.shane.android.videoplayer.bean.Video;
import com.shane.android.videoplayer.bean.VideoUrl;
import com.shane.android.videoplayer.engine.DLNAContainer;
import com.shane.android.videoplayer.engine.MP4HeaderCoder;
import com.shane.android.videoplayer.service.DLNAService;
import com.shane.android.videoplayer.util.DensityUtil;
import com.shane.android.videoplayer.util.FileUtil;
import com.shane.android.videoplayer.util.LogUtil;
import com.shane.android.videoplayer.widget.MediaController;
import com.shane.android.videoplayer.widget.SuperVideoPlayer;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.base.Status;
import org.wlf.filedownloader.listener.OnFileDownloadStatusListener;
import org.wlf.filedownloader.listener.simple.OnSimpleFileDownloadStatusListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SuperVideoPlayer mSuperVideoPlayer;
    private View mPlayBtnView;
    String remote1 = "http://114.55.231.90:1987/static/public/MP4/test1.mp4";
    String remote2 = "http://114.55.231.90:1987/static/public/MP4/test2.mp4";
    String remote3 = "http://114.55.231.90:1987/static/public/MP4/test3.mp4";
    String local = "/sdcard/test2.mp4";
    private HashMap<String, ArrayList<VideoUrl>> mapUrlVideo = new HashMap<String, ArrayList<VideoUrl>>();

    private SuperVideoPlayer.VideoPlayCallbackImpl mVideoPlayCallback = new SuperVideoPlayer.VideoPlayCallbackImpl() {
        @Override
        public void onCloseVideo() {
            mSuperVideoPlayer.close();
            mPlayBtnView.setVisibility(View.VISIBLE);
            mSuperVideoPlayer.setVisibility(View.GONE);
            resetPageToPortrait();
        }

        @Override
        public void onSwitchPageType() {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mSuperVideoPlayer.setPageType(MediaController.PageType.EXPAND);
            }
        }

        @Override
        public void onPlayFinish() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mSuperVideoPlayer = (SuperVideoPlayer) findViewById(R.id.video_player_item_1);
        mPlayBtnView = findViewById(R.id.play_btn);
        mPlayBtnView.setOnClickListener(this);
        mSuperVideoPlayer.setVideoPlayCallback(mVideoPlayCallback);


        FileDownloader.registerDownloadStatusListener(mOnFileDownloadStatusListener);
        mapUrlVideo.clear();
        startDLNAService();
    }

    private Video addUrl(String url, String name) {
        Video video = new Video();
        VideoUrl videoUrl1 = new VideoUrl();
        videoUrl1.setFormatName("720P");
        videoUrl1.setFormatUrl(url);
        VideoUrl videoUrl2 = new VideoUrl();
        videoUrl2.setFormatName("480P");
        videoUrl2.setFormatUrl(url);
        ArrayList<VideoUrl> arrayList1 = new ArrayList<>();
        arrayList1.add(videoUrl1);
        arrayList1.add(videoUrl2);
        video.setVideoName(name);
        video.setVideoUrl(arrayList1);
        mapUrlVideo.put(url, arrayList1);

        return video;
    }

    @Override
    public void onClick(View view) {
        mPlayBtnView.setVisibility(View.GONE);
        mSuperVideoPlayer.setVisibility(View.VISIBLE);
        mSuperVideoPlayer.setAutoHideController(true);

        ArrayList<Video> videoArrayList = new ArrayList<>();

        videoArrayList.add(addUrl(remote2, "test2"));
        videoArrayList.add(addUrl(remote3, "test3"));
        videoArrayList.add(addUrl(remote1, "test1"));

        mSuperVideoPlayer.loadMultipleVideo(videoArrayList,0,0,0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();;
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDLNAService();
        // pause all downloads
        FileDownloader.pauseAll();
        // unregisterDownloadStatusListener
        FileDownloader.unregisterDownloadStatusListener(mOnFileDownloadStatusListener);
    }

    /***
     * 旋转屏幕之后回调
     *
     * @param newConfig newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null == mSuperVideoPlayer) return;
        /***
         * 根据屏幕方向重新设置播放器的大小
         */
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().invalidate();
            float height = DensityUtil.getWidthInPx(this);
            float width = DensityUtil.getHeightInPx(this);
            mSuperVideoPlayer.getLayoutParams().height = (int) width;
            mSuperVideoPlayer.getLayoutParams().width = (int) height;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            float width = DensityUtil.getWidthInPx(this);
            float height = DensityUtil.dip2px(this, 200.f);
            mSuperVideoPlayer.getLayoutParams().height = (int) height;
            mSuperVideoPlayer.getLayoutParams().width = (int) width;
        }
    }


    /***
     * 恢复屏幕至竖屏
     */
    private void resetPageToPortrait() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
        }
    }

    private void startDLNAService() {
        // Clear the device container.
        DLNAContainer.getInstance().clear();
        Intent intent = new Intent(getApplicationContext(), DLNAService.class);
        startService(intent);
    }

    private void stopDLNAService() {
        Intent intent = new Intent(getApplicationContext(), DLNAService.class);
        stopService(intent);
    }

    private OnFileDownloadStatusListener mOnFileDownloadStatusListener = new OnSimpleFileDownloadStatusListener() {
        @Override
        public void onFileDownloadStatusRetrying(DownloadFileInfo downloadFileInfo, int retryTimes) {

        }

        @Override
        public void onFileDownloadStatusWaiting(DownloadFileInfo downloadFileInfo) {
            // waiting for download(wait for other tasks paused, or FileDownloader is busy for other operations)
        }

        @Override
        public void onFileDownloadStatusPreparing(DownloadFileInfo downloadFileInfo) {
            long fileSize = downloadFileInfo.getFileSizeLong();
            final String url = downloadFileInfo.getUrl();

            LogUtil.d(TAG, "size, onFileDownloadStatusPreparing:" + fileSize);

            boolean pause = false;
            if (fileSize > AppContext.MAX_CACHE_FILE_SIZE) {
                mSuperVideoPlayer.notifyFileDownloaderStatus(url, false, "");
                pause = true;
            }
            String encodeFile = AppContext.getEncodeFile(url);
            if (FileUtil.fileExists(encodeFile)) pause = true;
            if (pause) FileDownloader.pause(url);
            LogUtil.d(TAG, "onFileDownloadStatusPreparing, encodeFile:" + encodeFile);
        }

        @Override
        public void onFileDownloadStatusPrepared(DownloadFileInfo downloadFileInfo) {
            // prepared(connected)
            LogUtil.d(TAG, "size, onFileDownloadStatusPrepared:" + downloadFileInfo.getFileSizeLong());
        }

        @Override
        public void onFileDownloadStatusDownloading(DownloadFileInfo downloadFileInfo, float downloadSpeed, long
                remainingTime) {
            // downloading, the downloadSpeed with KB/s unit, the remainingTime with seconds unit
        }

        @Override
        public void onFileDownloadStatusPaused(DownloadFileInfo downloadFileInfo) {
            // download paused
        }

        @Override
        public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
            String url = downloadFileInfo.getUrl();
            String path = null;
            int status = downloadFileInfo.getStatus();
            LogUtil.d(TAG, "onFileDownloadStatusCompleted:" + status);

            String encodeFile = AppContext.getEncodeFile(url);
            LogUtil.d(TAG, "onFileDownloadStatusCompleted:encodeFile" + encodeFile);
            if (FileUtil.fileExists(encodeFile)) {
                path = encodeFile;
            } else if (status != Status.DOWNLOAD_STATUS_COMPLETED) {
                Toast.makeText(MainActivity.this, "DOWNLOAD_STATUS:"+status, Toast.LENGTH_SHORT).show();
                FileDownloader.reStart(url);
                return;
            }

            final String oldPath = downloadFileInfo.getFilePath();

            // download completed(the url file has been finished)
            ArrayList<VideoUrl> urls = mapUrlVideo.get(url);
            if (urls == null || urls.size() == 0) {
                return;
            } else {
                Toast.makeText(MainActivity.this, "downloadFileInfo:"+oldPath, Toast.LENGTH_SHORT).show();
                if (path == null) {
                    path = encodeFile;
                    MP4HeaderCoder mp4 = new MP4HeaderCoder(oldPath);
                    byte[] encodeData = mp4.getEncodeBytes(mp4.getRawBytes());
                    FileUtil.saveFileString(path, encodeData);
                }

                MP4HeaderCoder mp4 = new MP4HeaderCoder(path);
                byte[] decodeData = mp4.getDecodeBytes(mp4.getRawBytes());
                path = oldPath + "_decode.mp4";
                FileUtil.saveFileString(path, decodeData);

//                path = oldPath;
                for (VideoUrl vu: urls) {
                    vu.setIsDownloaded(true, path);
                }

                mSuperVideoPlayer.notifyFileDownloaderStatus(url, true, path);

                LogUtil.d(TAG, "notifyFileDownloaderStatus---1");
            }
        }


        @Override
        public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
            // error occur, see failReason for details, some of the failReason you must concern
            LogUtil.d(TAG, "onFileDownloadStatusFailed:" + downloadFileInfo.getStatus());
            String failType = failReason.getType();
            String failUrl = failReason.getUrl();// or failUrl = url, both url and failReason.getUrl() are the same

            if(FileDownloadStatusFailReason.TYPE_URL_ILLEGAL.equals(failType)){
                // the url error when downloading file with failUrl
            }else if(FileDownloadStatusFailReason.TYPE_STORAGE_SPACE_IS_FULL.equals(failType)){
                // storage space is full when downloading file with failUrl
            }else if(FileDownloadStatusFailReason.TYPE_NETWORK_DENIED.equals(failType)){
                // network access denied when downloading file with failUrl
            }else if(FileDownloadStatusFailReason.TYPE_NETWORK_TIMEOUT.equals(failType)){
                // connect timeout when downloading file with failUrl
            }else{
                // more....
            }

            // exception details
            Throwable failCause = failReason.getCause();// or failReason.getOriginalCause()

            // also you can see the exception message
            String failMsg = failReason.getMessage();// or failReason.getOriginalCause().getMessage()
        }
    };

}
