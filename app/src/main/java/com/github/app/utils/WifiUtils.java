package com.github.app.utils;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WifiUtils {

    public static WifiConfiguration getCurrentWifiConfiguration(WifiManager wifiManager) {
        if (!wifiManager.isWifiEnabled())
            return null;
        List<WifiConfiguration> configurationList = wifiManager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = wifiManager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur) {
                configuration = wifiConfiguration;
                break;
            }
        }
        return configuration;
    }

    public static Observable<ProxyInfo> getCurrentWifiProxyInfo(WifiManager wifiManager) {
        return Observable.create((Observable.OnSubscribe<ProxyInfo>) subscriber -> {
            if (subscriber.isUnsubscribed()) return;

            while (!wifiManager.isWifiEnabled()) {
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    subscriber.onError(e);
                }
            }

            WifiConfiguration configuration = getCurrentWifiConfiguration(wifiManager);
            if (configuration != null) {
                try {
                    ProxyInfo info = Whitebox.invokeMethod(configuration, "getHttpProxy");
                    subscriber.onNext(info);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static void setHttpPorxySetting(Context context, String host, int port) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = getCurrentWifiConfiguration(wifiManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ProxyInfo info = ProxyInfo.buildDirectProxy(host, port);

            if (configuration != null) {
                try {
                    Object mIpConfiguration = Whitebox.getInternalState(configuration, "mIpConfiguration");
                    Field field = mIpConfiguration.getClass().getField("proxySettings");
                    Whitebox.invokeMethod(configuration, "setProxy", field.getType().getEnumConstants()[ProxySetting.STATIC.ordinal()], info);

                    Whitebox.invokeMethod(wifiManager, "save", configuration, null);
                    wifiManager.disconnect();
                    wifiManager.reconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void unSetHttpProxy(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = getCurrentWifiConfiguration(wifiManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ProxyInfo info = ProxyInfo.buildDirectProxy(null, 0);

            if (configuration != null) {
                try {
                    Object mIpConfiguration = Whitebox.getInternalState(configuration, "mIpConfiguration");
                    Field field = mIpConfiguration.getClass().getField("proxySettings");
                    Whitebox.invokeMethod(configuration, "setProxy", field.getType().getEnumConstants()[ProxySetting.NONE.ordinal()], info);

                    Whitebox.invokeMethod(wifiManager, "save", configuration, null);
                    wifiManager.disconnect();
                    wifiManager.reconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private enum ProxySetting {
        /* No proxy is to be used. Any existing proxy settings
             * should be cleared. */
        NONE,
        /* Use statically configured proxy. Configuration can be accessed
         * with httpProxy. */
        STATIC,
        /* no proxy details are assigned, this is used to indicate
         * that any existing proxy settings should be retained */
        UNASSIGNED,
        /* Use a Pac based proxy.
         */
        PAC
    }

}
