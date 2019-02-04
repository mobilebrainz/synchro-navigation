package app.mobilebrainz.synchronavigatin.navigation;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

import app.mobilebrainz.synchronavigatin.viewmodels.event.SingleLiveEvent;


public class BundleViewModel extends ViewModel {

    private SingleLiveEvent<Bundle> bundle = new SingleLiveEvent<>();

    public void setBundle(Bundle bundle) {
        if (!equalsBundles(this.bundle.getValue(), bundle)) {
            this.bundle.setValue(bundle);
        }
    }

    public void observe(LifecycleOwner owner, final Observer<Bundle> observer) {
        bundle.observe(owner, observer);
    }

    /**
     * https://stackoverflow.com/questions/13234994/check-if-two-bundle-objects-are-equal-in-android
     */
    private boolean equalsBundles(Bundle a, Bundle b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        Set<String> aks = a.keySet();
        Set<String> bks = b.keySet();
        if (!aks.containsAll(bks)) {
            return false;
        }
        for (String key : aks) {
            Object obj1 = a.get(key);
            Object obj2 = b.get(key);
            if (obj1 == null && obj2 == null) {
                continue;
            }
            if (obj1 == null || obj2 == null) {
                return false;
            }
            if (!obj1.equals(obj2)) {
                return false;
            }
            if (obj1 instanceof Bundle && obj2 instanceof Bundle
                    && !equalsBundles((Bundle) obj1, (Bundle) obj2)) {
                return false;
            }
        }
        return true;
    }

}
