package com.github.mirror.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "selected_proxy",
        foreignKeys = @ForeignKey(
                entity = ProxyConfig.class,
                parentColumns = "id",
                childColumns = "configId",
                onDelete = CASCADE))
public class SelectedProxy {
    @PrimaryKey
    public int configId;
    private String host;
    private int port;

    public SelectedProxy(int configId, String host, int port) {
        this.configId = configId;
        this.host = host;
        this.port = port;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConfigId() {
        return configId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "[" + host + "]" + " : " + port;
    }

    public static SelectedProxy wrap(ProxyConfig proxyConfig) {
        return new SelectedProxy(proxyConfig.id, proxyConfig.getHost(), proxyConfig.getPort());
    }
}


