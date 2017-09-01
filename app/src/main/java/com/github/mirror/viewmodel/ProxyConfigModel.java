package com.github.mirror.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.github.mirror.db.AppDatabase;
import com.github.mirror.db.entity.ProxyConfig;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class ProxyConfigModel extends ViewModel {
    private AppDatabase mAppDatabase;

    public ProxyConfigModel(AppDatabase appDatabase) {
        mAppDatabase = appDatabase;
    }

    public LiveData<List<ProxyConfig>> getAllProxyConfig() {
        return mAppDatabase.proxyDao().fetchAll();
    }

    public void insert(ProxyConfig config) {
        Completable.fromAction(() -> mAppDatabase.proxyDao().insertProxyConfig(config)).subscribeOn(Schedulers.io()).subscribe();
    }

    public void update(ProxyConfig config) {
        Completable.fromAction(() -> mAppDatabase.proxyDao().updateProxyConfig(config)).subscribeOn(Schedulers.io()).subscribe();
    }

    public void delete(ProxyConfig config) {
        Completable.fromAction(() -> mAppDatabase.proxyDao().deleteProxyConfig(config)).subscribeOn(Schedulers.io()).subscribe();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private AppDatabase mAppDatabase;

        @Inject
        public Factory(AppDatabase appDatabase) {
            mAppDatabase = appDatabase;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new ProxyConfigModel(mAppDatabase);
        }
    }
}
