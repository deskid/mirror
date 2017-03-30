package com.github.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Proxy;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.app.utils.WifiUtils;

public class WifiFragment extends Fragment {

    public static final String EMPTY_PROXY_INFO = "[0.0.0.0] 0";
    WifiManager mWifiManager;
    private BroadcastReceiver mBroadcastReceiver;
    private ToggleButton mToggleButton;
    private TextView mProxyTextView;
    private ProgressDialog mProgressDialog;

    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener;

    public WifiFragment() {
    }

    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(getContext());
        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    SharedPreferences sharedPreferences = WifiFragment.this.getContext().getSharedPreferences("CACHE", Context.MODE_PRIVATE);
                    String lastHost = sharedPreferences.getString("host", "");
                    int lastPort = sharedPreferences.getInt("port", 0);

                    if (!TextUtils.isEmpty(lastHost) && lastPort != 0) {
                        WifiUtils.setHttpPorxySetting(WifiFragment.this.getContext(), lastHost, lastPort);
                        mProgressDialog.show();
                    } else {
                        Toast.makeText(WifiFragment.this.getContext(),
                                R.string.set_proxy_notice, Toast.LENGTH_SHORT).show();
                        buttonView.setOnCheckedChangeListener(null);
                        buttonView.setChecked(false);
                        buttonView.setOnCheckedChangeListener(this);
                    }

                } else {
                    WifiUtils.unSetHttpProxy(WifiFragment.this.getContext());
                    mProgressDialog.show();
                }
            }
        };

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                ProxyInfo proxyInfo = intent.getParcelableExtra(Proxy.EXTRA_PROXY_INFO);

                mToggleButton.setOnCheckedChangeListener(null);
                if (!TextUtils.isEmpty(proxyInfo.getHost())) {
                    mToggleButton.setChecked(true);
                    updateProxyInfo(proxyInfo.toString());
                } else {
                    mToggleButton.setChecked(false);
                    updateProxyInfo(EMPTY_PROXY_INFO);
                }
                mToggleButton.setOnCheckedChangeListener(mCheckedChangeListener);
                mProgressDialog.hide();
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        WifiUtils.getCurrentWifiProxyInfo(mWifiManager)
                .subscribe(proxyInfo -> {
                    mToggleButton.setOnCheckedChangeListener(null);
                    if (proxyInfo != null && proxyInfo.getHost() != null) {
                        mToggleButton.setChecked(true);
                        updateProxyInfo(proxyInfo.toString());
                    } else {
                        mToggleButton.setChecked(false);
                        updateProxyInfo(EMPTY_PROXY_INFO);
                    }
                    mToggleButton.setOnCheckedChangeListener(mCheckedChangeListener);
                });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Proxy.PROXY_CHANGE_ACTION);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wifi, container, false);
        mToggleButton = (ToggleButton) root.findViewById(R.id.toggleButton);
        mToggleButton.setTextOff(getString(R.string.toggle_btn_off));
        mToggleButton.setTextOn(getString(R.string.toggle_btn_on));

        WifiConfiguration configuration = WifiUtils.getCurrentWifiConfiguration(mWifiManager);
        TextView ssid = (TextView) root.findViewById(R.id.ssid);
        if (configuration != null) {
            ssid.setText(configuration.SSID);
        }

        mProxyTextView = (TextView) root.findViewById(R.id.proxy);
        mToggleButton.setOnCheckedChangeListener(mCheckedChangeListener);
        return root;
    }

    private void updateProxyInfo(final String proxyInfo) {
        if (!EMPTY_PROXY_INFO.equals(proxyInfo)) {
            mProxyTextView.setText(proxyInfo);
            mProxyTextView.setEnabled(true);
        } else {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CACHE", Context.MODE_PRIVATE);
            String lastHost = sharedPreferences.getString("host", "");
            int lastPort = sharedPreferences.getInt("port", 0);

            mProxyTextView.setText(getString(R.string.host_port_text, lastHost, lastPort));
            mProxyTextView.setEnabled(false);
        }
    }
}
