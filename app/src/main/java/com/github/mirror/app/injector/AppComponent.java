package com.github.mirror.app.injector;

import com.github.mirror.ui.CurrentWifiInfoFragment;
import com.github.mirror.ui.ProxyConfigListFragment;

import dagger.Component;

@AppScope
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(CurrentWifiInfoFragment currentWifiInfoFragment);

    void inject(ProxyConfigListFragment proxyConfigListFragment);
}
