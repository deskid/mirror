package com.github.mirror.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.github.mirror.db.entity.ProxyConfig;
import com.github.mirror.db.entity.SelectedProxy;

import java.util.List;

@Dao
public interface ProxyDao {
    @Query("select * from proxy_config")
    LiveData<List<ProxyConfig>> fetchAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProxyConfig(ProxyConfig config);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateProxyConfig(ProxyConfig config);

    @Delete
    void deleteProxyConfig(ProxyConfig config);

    @Query("select * from selected_proxy")
    LiveData<List<SelectedProxy>> getSelected();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setSelectedProxy(SelectedProxy config);

    @Query("DELETE FROM selected_proxy")
    void cleanSelectedProxy();

}
