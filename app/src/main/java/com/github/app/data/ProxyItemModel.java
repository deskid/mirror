package com.github.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhou on 2/16/17.
 * use SharedPreferences as persistence layer
 */

public class ProxyItemModel implements Collection<ProxyItem> {

    private static final String CREATE_SHAREPREF_NAME = "PROXY_ITEMS";
    private final SharedPreferences mSharedPreferences;

    private List<ProxyItem> mItems = new ArrayList<>();
    private int mSelectedIndex = 0;

    private Gson mGson = new Gson();

    public ProxyItemModel(Context context) {
        mSharedPreferences = context.getSharedPreferences(CREATE_SHAREPREF_NAME, Context.MODE_PRIVATE);
        String json = mSharedPreferences.getString("items", mGson.toJson(new ArrayList<>()));
        final Type type = new TypeToken<List<ProxyItem>>() {
        }.getType();
        mItems = mGson.fromJson(json, type);
        mSelectedIndex = initSelectedIndex(mItems);
    }

    private int initSelectedIndex(List<ProxyItem> items) {
        int selectedIndex = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                mSelectedIndex = i;
                break;
            }
        }
        return selectedIndex;
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return mItems.contains(o);
    }

    @NonNull
    @Override
    public Iterator<ProxyItem> iterator() {
        return mItems.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mItems.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull final T[] a) {
        return mItems.toArray(a);
    }

    @Override
    public boolean add(ProxyItem item) {
        boolean result = mItems.add(item);
        commit();
        return result;
    }

    @Override
    public boolean remove(final Object o) {
        boolean result = mItems.remove(o);
        commit();
        return result;
    }

    @Override
    public boolean containsAll(@NonNull final Collection<?> c) {
        return mItems.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull final Collection<? extends ProxyItem> c) {
        boolean result = mItems.addAll(c);
        commit();
        return result;
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {
        boolean result = mItems.removeAll(c);
        commit();
        return result;
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> c) {
        boolean result = mItems.retainAll(c);
        commit();
        return result;
    }

    @Override
    public void clear() {
        mItems.clear();
        commit();
    }

    private void commit() {
        mSharedPreferences.edit().putString("items", mGson.toJson(mItems)).apply();
    }

    public void delete(int index) {
        mItems.remove(index);
        commit();
    }

    public void update(int index, ProxyItem item) {
        if (item == null) return;
        mItems.set(index, item);
        verifySelected(index, item);
        commit();
    }

    private void verifySelected(int index, ProxyItem item) {
        if (item.isSelected() && index != mSelectedIndex) {
            if (mItems.get(mSelectedIndex).isSelected()) {
                mItems.get(mSelectedIndex).setSelected(false);
                mSelectedIndex = index;
            }
        }
    }

    public boolean contains(String host, int port) {
        ProxyItem item = new ProxyItem(host, port);
        return mItems.contains(item);
    }

    public ProxyItem get(int index) {
        return mItems.get(index);
    }

    public List<ProxyItem> fetch() {
        return mItems;
    }
}
