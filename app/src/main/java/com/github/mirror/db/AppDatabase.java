package com.github.mirror.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.github.mirror.db.entity.ProxyConfig;
import com.github.mirror.db.entity.SelectedProxy;

@Database(entities = {ProxyConfig.class, SelectedProxy.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "wifi_proxy_db";

    public abstract ProxyDao proxyDao();
}
