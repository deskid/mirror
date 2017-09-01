package com.github.mirror.app.injector;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.github.mirror.db.AppDatabase;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Context context;

    public AppModule(final Context context) {
        this.context = context;
    }

    @AppScope
    @Provides
    public Context provideContext() {
        return context;
    }

    @AppScope
    @Provides
    public WifiManager provideWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @AppScope
    @Provides
    public AppDatabase provideAppDatabase() {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, AppDatabase.DATABASE_NAME).build();
    }
}
