package app.mobilebrainz.synchronavigatin.navigation;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;

import app.mobilebrainz.synchronavigatin.viewmodels.event.SingleLiveEvent;


public class BundleViewModel extends ViewModel {

    private SingleLiveEvent<Bundle> bundle = new SingleLiveEvent<>();

    public void setBundle(Bundle bundle) {
        this.bundle.setValue(bundle);
    }

    public void observe(LifecycleOwner owner, final Observer<Bundle> observer) {
        bundle.observe(owner, observer);
    }

}
