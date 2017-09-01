package com.github.mirror.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "proxy_config", indices = {@Index(value = {"host", "port"}, unique = true)})
public class ProxyConfig {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String host;
    private int port;

    public ProxyConfig(final int id, final String host, final int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ProxyConfig proxyItem = (ProxyConfig) o;

        return port == proxyItem.port && host.equals(proxyItem.host);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
