package com.github.mirror.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.github.mirror.db.AppDatabase;
import com.github.mirror.db.entity.SelectedProxy;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class SelectedProxyModel extends ViewModel {
    private AppDatabase mAppDatabase;

    public SelectedProxyModel(AppDatabase appDatabase) {
        mAppDatabase = appDatabase;
    }

    public LiveData<SelectedProxy> getSelectedProxy() {
        return Transformations.map(mAppDatabase.proxyDao().getSelected(), input -> {
            if (input.isEmpty()) {
                return null;
            }
            return input.get(0);
        });
    }

    public void updateSelectedProxy(SelectedProxy proxy) {
        Completable.fromAction(() -> {
            mAppDatabase.proxyDao().cleanSelectedProxy();
            mAppDatabase.proxyDao().setSelectedProxy(proxy);
        }).subscribeOn(Schedulers.io()).subscribe();
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
            return (T) new SelectedProxyModel(mAppDatabase);
        }
    }

}
