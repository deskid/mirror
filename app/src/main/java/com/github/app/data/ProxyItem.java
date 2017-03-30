package com.github.app.data;

public class ProxyItem {
    private String host;
    private int port;
    private boolean selected;

    public ProxyItem(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
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

        final ProxyItem proxyItem = (ProxyItem) o;

        return port == proxyItem.port && host.equals(proxyItem.host);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
