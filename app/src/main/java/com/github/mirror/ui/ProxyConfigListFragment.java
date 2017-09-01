package com.github.mirror.ui;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mirror.R;
import com.github.mirror.app.App;
import com.github.mirror.db.entity.ProxyConfig;
import com.github.mirror.db.entity.SelectedProxy;
import com.github.mirror.viewmodel.ProxyConfigModel;
import com.github.mirror.viewmodel.SelectedProxyModel;

import javax.inject.Inject;

public class ProxyConfigListFragment extends LifecycleFragment {

    @Inject
    ProxyConfigModel.Factory mProxyConfigFactory;

    @Inject
    SelectedProxyModel.Factory mSelectedProxyFactory;

    private ProxyConfigModel mProxyConfigModel;
    private SelectedProxyModel mSelectedProxyModel;

    private ProxyItemsAdapter mAdapter;

    private AlertDialog mDeleteProxyItemDialog;
    private AlertDialog mUpdateProxyItemDialog;
    private AlertDialog mNewProxyItemDialog;

    public ProxyConfigListFragment() {
    }

    public static ProxyConfigListFragment newInstance() {
        return new ProxyConfigListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) (getActivity().getApplication())).getAppComponent().inject(this);
        mSelectedProxyModel = ViewModelProviders.of(getActivity(), mSelectedProxyFactory).get(SelectedProxyModel.class);
        mProxyConfigModel = ViewModelProviders.of(getActivity(), mProxyConfigFactory).get(ProxyConfigModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        mProxyConfigModel.getAllProxyConfig().observe(this, configs -> mAdapter.swipeDate(configs));

        mSelectedProxyModel.getSelectedProxy().observe(this, proxy -> mAdapter.setSelectedId(proxy != null ? proxy.configId : -1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_proxy_config_list, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.list);

        mAdapter = new ProxyItemsAdapter();

        mAdapter.getPositiOnLongClicks().subscribe(pair -> showUpdateProxyItemDialog(pair.first, pair.second));
        mAdapter.getPositiOnClicks().subscribe(pair -> mSelectedProxyModel.updateSelectedProxy(SelectedProxy.wrap(pair.second)));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                int position = viewHolder.getAdapterPosition();
                ProxyConfig item = mAdapter.getItemData(position);
                if (item != null) {
                    showDeleteProxyItemDialog(item);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(view -> showNewProxyItemDialog());
        return root;
    }

    //todo abstract a method to show dialog
    private void showUpdateProxyItemDialog(final int index, final ProxyConfig item) {
        if (mUpdateProxyItemDialog == null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_proxy_config, null);
            mUpdateProxyItemDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("修改代理")
                    .setView(dialogView)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("保存", null).create();
        }

        mUpdateProxyItemDialog.setOnShowListener(dialog -> {
            EditText hostEt = (EditText) mUpdateProxyItemDialog.findViewById(R.id.host);
            EditText portEt = (EditText) mUpdateProxyItemDialog.findViewById(R.id.port);

            if (hostEt == null || portEt == null) return;
            hostEt.setText(item.getHost());
            portEt.setText(String.valueOf(item.getPort()));

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {

                if (TextUtils.isEmpty(hostEt.getText()) || TextUtils.isEmpty(portEt.getText())) {
                    Toast.makeText(getContext(), "请填写完整的代理信息", Toast.LENGTH_SHORT).show();
                } else {
                    item.setHost(hostEt.getText().toString());
                    item.setPort(Integer.valueOf(portEt.getText().toString()));
                    mProxyConfigModel.update(item);
                    dialog.dismiss();
                }
            });
        });

        if (!mUpdateProxyItemDialog.isShowing()) {
            mUpdateProxyItemDialog.show();
        }
    }

    private void showDeleteProxyItemDialog(final ProxyConfig item) {
        if (mDeleteProxyItemDialog == null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_proxy_config, null);
            mDeleteProxyItemDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("删除代理？")
                    .setView(dialogView)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确认", null).create();
        }

        mDeleteProxyItemDialog.setOnShowListener(dialog -> {
            EditText hostEt = (EditText) mDeleteProxyItemDialog.findViewById(R.id.host);
            EditText portEt = (EditText) mDeleteProxyItemDialog.findViewById(R.id.port);

            if (hostEt == null || portEt == null) return;
            hostEt.setText(item.getHost());
            hostEt.setEnabled(false);
            portEt.setText(String.valueOf(item.getPort()));
            portEt.setEnabled(false);

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                mProxyConfigModel.delete(item);
                dialog.dismiss();

            });
            Button button2 = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            button2.setOnClickListener(view -> {
                //cancel the delete
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
        });

        if (!mDeleteProxyItemDialog.isShowing()) {
            mDeleteProxyItemDialog.show();
        }
    }

    private void showNewProxyItemDialog() {
        if (mNewProxyItemDialog == null) {

            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_proxy_config, null);

            mNewProxyItemDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("新建代理")
                    .setView(dialogView)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("保存", null).create();
        }

        mNewProxyItemDialog.setOnShowListener(dialog -> {

            EditText hostEt = (EditText) mNewProxyItemDialog.findViewById(R.id.host);
            EditText portEt = (EditText) mNewProxyItemDialog.findViewById(R.id.port);

            if (hostEt == null || portEt == null) return;
            hostEt.setText("");
            portEt.setText("");
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {

                if (TextUtils.isEmpty(hostEt.getText()) || TextUtils.isEmpty(portEt.getText())) {
                    Toast.makeText(getContext(), "请填写完整的代理信息", Toast.LENGTH_SHORT).show();
                } else {
                    mProxyConfigModel.insert(new ProxyConfig(0, hostEt.getText().toString(), Integer.valueOf(portEt.getText().toString())));
                    dialog.dismiss();
                }
            });
        });

        if (!mNewProxyItemDialog.isShowing()) {
            mNewProxyItemDialog.show();
        }
    }

}
