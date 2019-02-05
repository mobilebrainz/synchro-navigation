package app.mobilebrainz.synchronavigatin.navigation;


import android.os.Bundle;

public interface UpdatableFragmentInterface {

    BundleViewModel getBundleViewModel();

    void update(Bundle bundle);

}
