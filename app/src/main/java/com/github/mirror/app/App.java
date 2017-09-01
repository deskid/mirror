package com.github.mirror.app;

import android.app.Application;

import com.github.logutils.DebugUtils;
import com.github.mirror.app.injector.AppComponent;
import com.github.mirror.app.injector.AppModule;
import com.github.mirror.app.injector.DaggerAppComponent;

public class App extends Application {
    AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        DebugUtils.setApplicationContext(getApplicationContext());

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
