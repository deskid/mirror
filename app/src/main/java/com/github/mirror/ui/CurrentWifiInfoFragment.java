package com.github.mirror.ui;

import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Proxy;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mirror.R;
import com.github.mirror.app.App;
import com.github.mirror.utils.WifiUtils;
import com.github.mirror.viewmodel.SelectedProxyModel;

import javax.inject.Inject;

public class CurrentWifiInfoFragment extends LifecycleFragment {

    private static final String EMPTY_PROXY_INFO = "[Direct]";

    @Inject
    WifiManager mWifiManager;

    @Inject
    SelectedProxyModel.Factory mFactory;

    private SelectedProxyModel mSelectedProxyModel;
    private BroadcastReceiver mBroadcastReceiver;

    private ToggleButton mToggleButton;
    private TextView mCurrentProxyTextView;
    private TextView mSelectedProxyTextView;
    private ProgressDialog mProgressDialog;

    private OnCheckedChangeListener mSwitchChangedListener;
    private TextView mSsid;

    public CurrentWifiInfoFragment() {
    }

    public static CurrentWifiInfoFragment newInstance() {
        return new CurrentWifiInfoFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) (getActivity().getApplication())).getAppComponent().inject(this);
        mSelectedProxyModel = ViewModelProviders.of(getActivity(), mFactory).get(SelectedProxyModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentWifiInfo();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Proxy.PROXY_CHANGE_ACTION);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void updateCurrentWifiInfo() {
        WifiConfiguration configuration = WifiUtils.getCurrentWifiConfiguration(mWifiManager);

        if (configuration != null) {
            mSsid.setText(configuration.SSID);
        }

        WifiUtils.getCurrentWifiProxyInfo(mWifiManager)
                .subscribe(proxyInfo -> {
                    mToggleButton.setOnCheckedChangeListener(null);
                    if (proxyInfo != null && !TextUtils.isEmpty(WifiUtils.getHost(proxyInfo))) {
                        mToggleButton.setChecked(true);
                        updateCurrentProxyInfo(proxyInfo.toString());
                    } else {
                        mToggleButton.setChecked(false);
                        updateCurrentProxyInfo(EMPTY_PROXY_INFO);
                    }
                    mToggleButton.setOnCheckedChangeListener(mSwitchChangedListener);
                }, throwable -> Toast.makeText(getContext(), R.string.set_proxy_notice, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_current_proxy_config, container, false);

        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            updateCurrentWifiInfo();
        });

        mCurrentProxyTextView = root.findViewById(R.id.proxy);
        mSelectedProxyTextView = root.findViewById(R.id.proxy_selected);
        mSsid = root.findViewById(R.id.ssid);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mToggleButton = root.findViewById(R.id.toggleButton);
        mToggleButton.setTextOff(getString(R.string.toggle_btn_off));
        mToggleButton.setTextOn(getString(R.string.toggle_btn_on));

        mSwitchChangedListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    mSelectedProxyModel.getSelectedProxy().observe(CurrentWifiInfoFragment.this, selectedProxy -> {
                        if (selectedProxy == null) {
                            Toast.makeText(getContext(), R.string.set_proxy_notice, Toast.LENGTH_SHORT).show();
                            buttonView.setOnCheckedChangeListener(null);
                            buttonView.setChecked(false);
                            buttonView.setOnCheckedChangeListener(this);
                        } else {
                            WifiUtils.setHttpPorxySetting(CurrentWifiInfoFragment.this.getContext(), selectedProxy.getHost(), selectedProxy.getPort());
                            mProgressDialog.show();
                            queryCurrentProxyInfo();
                        }
                    });
                } else {
                    WifiUtils.unSetHttpProxy(CurrentWifiInfoFragment.this.getContext());
                    mProgressDialog.show();
                    buttonView.postDelayed(() -> queryCurrentProxyInfo(), 1000);

                }
            }
        };
        mToggleButton.setOnCheckedChangeListener(mSwitchChangedListener);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                queryCurrentProxyInfo();
            }
        };

        mSelectedProxyModel.getSelectedProxy().observe(this, selectedProxy -> {
            if (selectedProxy == null) {
                Toast.makeText(getContext(), R.string.set_proxy_notice, Toast.LENGTH_SHORT).show();
                mSelectedProxyTextView.setText(EMPTY_PROXY_INFO);
            } else {
                mSelectedProxyTextView.setText(selectedProxy.toString());
            }
        });

        return root;
    }

    private void queryCurrentProxyInfo() {
        WifiUtils.getCurrentWifiProxyInfo(mWifiManager)
                .subscribe(proxyInfo -> {
                    mProgressDialog.hide();
                    mToggleButton.setOnCheckedChangeListener(null);
                    if (proxyInfo != null && !TextUtils.isEmpty(WifiUtils.getHost(proxyInfo))) {
                        mToggleButton.setChecked(true);
                        updateCurrentProxyInfo(proxyInfo.toString());
                    } else {
                        mToggleButton.setChecked(false);
                        updateCurrentProxyInfo(EMPTY_PROXY_INFO);
                    }
                    mToggleButton.setOnCheckedChangeListener(mSwitchChangedListener);
                }, throwable -> {
                    Toast.makeText(getContext(), R.string.set_proxy_notice, Toast.LENGTH_SHORT).show();
                    mProgressDialog.hide();
                });
    }

    private void updateCurrentProxyInfo(final String proxyInfo) {
        mCurrentProxyTextView.setText(proxyInfo);
    }
}
