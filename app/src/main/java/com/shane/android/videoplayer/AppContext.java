package com.shane.android.videoplayer;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.danikula.videocache.HttpProxyCacheServer;
import com.squareup.leakcanary.LeakCanary;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 2016-10-24
 */

public class AppContext extends Application {

    private static Context sContext;
    private static Resources sResource;
    private HttpProxyCacheServer mProxy;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sResource = sContext.getResources();
        LeakCanary.install(this);
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
