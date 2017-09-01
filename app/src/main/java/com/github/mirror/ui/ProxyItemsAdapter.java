package com.github.mirror.ui;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mirror.R;
import com.github.mirror.db.entity.ProxyConfig;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

class ProxyItemsAdapter extends RecyclerView.Adapter<ProxyItemsAdapter.ViewHolder> {

    private List<ProxyConfig> mConfigs = new ArrayList<>();
    private int mSelectedId;
    private final PublishSubject<Pair<Integer, ProxyConfig>> mOnClickSubject = PublishSubject.create();
    private PublishSubject<Pair<Integer, ProxyConfig>> mOnLongClickSubject = PublishSubject.create();

    Flowable<Pair<Integer, ProxyConfig>> getPositiOnClicks() {
        return mOnClickSubject.toFlowable(BackpressureStrategy.DROP);
    }

    Flowable<Pair<Integer, ProxyConfig>> getPositiOnLongClicks() {
        return mOnLongClickSubject.toFlowable(BackpressureStrategy.DROP);
    }

    public void swipeDate(List<ProxyConfig> configs) {
        mConfigs.clear();
        mConfigs.addAll(configs);
        notifyDataSetChanged();
    }

    public void setSelectedId(int selectedId) {
        mSelectedId = selectedId;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proxy_config, parent, false);

        return new ViewHolder(view, mOnClickSubject, mOnLongClickSubject);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ProxyConfig item = mConfigs.get(holder.getAdapterPosition());
        holder.bind(item, mSelectedId);
    }

    @Override
    public int getItemCount() {
        return mConfigs.size();
    }

    @Nullable
    public ProxyConfig getItemData(int position) {
        if (position >= 0 && position < mConfigs.size()) {
            return mConfigs.get(position);
        } else {
            return null;
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final TextView mContentView;
        final ImageView mCheckImg;

        ProxyConfig mItem;

        ViewHolder(View view, PublishSubject<Pair<Integer, ProxyConfig>> onClickSubject, PublishSubject<Pair<Integer, ProxyConfig>> onLongClickSubject) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.id);
            mContentView = view.findViewById(R.id.content);
            mCheckImg = view.findViewById(R.id.checkbox);
            mView.setOnClickListener(v -> onClickSubject.onNext(Pair.create(getAdapterPosition(), mItem)));
            mView.setOnLongClickListener(v -> {
                onLongClickSubject.onNext(Pair.create(getAdapterPosition(), mItem));
                return true;
            });
        }

        void bind(ProxyConfig item, int selectedId) {
            mItem = item;
            mIdView.setText(item.getHost());
            mContentView.setText(String.valueOf(item.getPort()));
            mCheckImg.setSelected(item.id == selectedId);
        }
    }
}
