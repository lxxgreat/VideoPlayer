package com.shane.android.videoplayer;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.danikula.videocache.HttpProxyCacheServer;
import com.shane.android.videoplayer.util.LogUtil;
import com.shane.android.videoplayer.widget.SuperVideoPlayer;
import com.squareup.leakcanary.LeakCanary;

import org.wlf.filedownloader.FileDownloadConfiguration;
import org.wlf.filedownloader.FileDownloader;

import java.io.File;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 2016-10-24
 */

public class AppContext extends Application {
    private static final String TAG = AppContext.class.getSimpleName();

    public static final int MAX_CACHE_FILE_SIZE = 1024 * 1024 * 200; // 200MB
    private static Context sContext;
    private static Resources sResource;
    private HttpProxyCacheServer mProxy;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sResource = sContext.getResources();
        LeakCanary.install(this);

        // init FileDownloader
        initFileDownloader();
    }



    public static HttpProxyCacheServer getProxy(Context context) {
        AppContext app = (AppContext) context.getApplicationContext();
        return app.mProxy == null ? (app.mProxy = app.newProxy()) : app.mProxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // release FileDownloader
        releaseFileDownloader();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    // init FileDownloader
    private void initFileDownloader() {

        // 1.create FileDownloadConfiguration.Builder
        FileDownloadConfiguration.Builder builder = new FileDownloadConfiguration.Builder(this);

        // 2.config FileDownloadConfiguration.Builder
        String cacheDir = getFilesDir().getAbsolutePath() + File.separator + "cached_videos";
        builder.configFileDownloadDir(cacheDir); // config the download path
        LogUtil.d(TAG, "cacheDir:" + cacheDir);

        // allow 3 download tasks at the same time
        builder.configDownloadTaskSize(3);

        // config retry download times when failed
        builder.configRetryDownloadTimes(5);

        // enable debug mode
        //builder.configDebugMode(true);

        // config connect timeout
        builder.configConnectTimeout(25000); // 25s

        // 3.init FileDownloader with the configuration
        FileDownloadConfiguration configuration = builder.build();
        FileDownloader.init(configuration);
    }

    // release FileDownloader
    private void releaseFileDownloader() {
        FileDownloader.release();
    }
}
