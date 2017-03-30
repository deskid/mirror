package com.github.app;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.app.data.ProxyItem;
import com.github.app.data.ProxyItemModel;

import rx.Observable;
import rx.subjects.PublishSubject;

class ProxyItemsAdapter extends RecyclerView.Adapter<ProxyItemsAdapter.ViewHolder> {

    private ProxyItemModel mModel;
    private final PublishSubject<Pair<Integer, ProxyItem>> mOnClickSubject = PublishSubject.create();
    private PublishSubject<Pair<Integer, ProxyItem>> mOnLongClickSubject = PublishSubject.create();

    Observable<Pair<Integer, ProxyItem>> getPositiOnClicks() {
        return mOnClickSubject.asObservable();
    }

    Observable<Pair<Integer, ProxyItem>> getPositiOnLongClicks() {
        return mOnLongClickSubject.asObservable();
    }

    ProxyItemsAdapter(ProxyItemModel model) {
        mModel = model;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_wifi_item, parent, false);

        return new ViewHolder(view, mOnClickSubject, mOnLongClickSubject);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ProxyItem item = mModel.get(holder.getAdapterPosition());
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final TextView mContentView;
        final ImageView mCheckImg;

        ProxyItem mItem;

        ViewHolder(View view, PublishSubject<Pair<Integer, ProxyItem>> onClickSubject, PublishSubject<Pair<Integer, ProxyItem>> onLongClickSubject) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCheckImg = (ImageView) view.findViewById(R.id.checkbox);
            mView.setOnClickListener(v -> onClickSubject.onNext(Pair.create(getAdapterPosition(), mItem)));
            mView.setOnLongClickListener(v -> {
                onLongClickSubject.onNext(Pair.create(getAdapterPosition(), mItem));
                return true;
            });
        }

        void bind(ProxyItem item) {
            mItem = item;
            mIdView.setText(item.getHost());
            mContentView.setText(String.valueOf(item.getPort()));
            mCheckImg.setSelected(item.isSelected());
        }
    }
}
