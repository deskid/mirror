package com.github.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.app.data.ProxyItem;
import com.github.app.data.ProxyItemModel;

public class ProxyItemsFragment extends Fragment {

    private ProxyItemModel mModel;
    private ProxyItemsAdapter mAdapter;
    private AlertDialog mDeleteProxyItemDialog;
    private AlertDialog mUpdateProxyItemDialog;
    private AlertDialog mNewProxyItemDialog;

    public ProxyItemsFragment() {
    }

    public static ProxyItemsFragment newInstance() {
        return new ProxyItemsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ProxyItemModel(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_proxy_items, container, false);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.list);

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        mAdapter = new ProxyItemsAdapter(mModel);
        mAdapter.getPositiOnLongClicks().subscribe(pair -> showUpdateProxyItemDialog(pair.first, pair.second));
        mAdapter.getPositiOnClicks().subscribe(pair -> {
            int index = pair.first;
            ProxyItem item = pair.second;

            item.setSelected(!item.isSelected());
            mModel.update(index, item);
            mAdapter.notifyDataSetChanged();

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("CACHE", Context.MODE_PRIVATE);
            if (item.isSelected()) {
                sharedPreferences.edit().putString("host", item.getHost()).apply();
                sharedPreferences.edit().putInt("port", item.getPort()).apply();
            } else {
                sharedPreferences.edit().clear().apply();
            }

        });
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                int position = viewHolder.getAdapterPosition();
                ProxyItem item = mModel.get(position);
                showDeleteProxyItemDialog(position, item);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(view -> showNewProxyItemDialog());
        return root;
    }

    //todo abstract a method to show dialog
    private void showUpdateProxyItemDialog(final int index, final ProxyItem item) {
        if (mUpdateProxyItemDialog == null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.new_wifi_item, null);
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
                    mModel.update(index, item);
                    mAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
        });

        if (!mUpdateProxyItemDialog.isShowing()) {
            mUpdateProxyItemDialog.show();
        }
    }

    private void showDeleteProxyItemDialog(final int index, final ProxyItem item) {
        if (mDeleteProxyItemDialog == null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.new_wifi_item, null);
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

                mModel.delete(index);
                mAdapter.notifyItemRemoved(index);
                dialog.dismiss();

            });
            Button button2 = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            button2.setOnClickListener(view -> {
                mAdapter.notifyItemChanged(index);
                dialog.dismiss();
            });
        });

        if (!mDeleteProxyItemDialog.isShowing()) {
            mDeleteProxyItemDialog.show();
        }
    }

    private void showNewProxyItemDialog() {
        if (mNewProxyItemDialog == null) {

            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.new_wifi_item, null);

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
                    mModel.add(new ProxyItem(hostEt.getText().toString(), Integer.valueOf(portEt.getText().toString())));
                    mAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
        });

        if (!mNewProxyItemDialog.isShowing()) {
            mNewProxyItemDialog.show();
        }
    }

}
