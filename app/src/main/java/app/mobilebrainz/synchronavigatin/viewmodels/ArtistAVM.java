package app.mobilebrainz.synchronavigatin.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;


public class ArtistAVM extends ViewModel {

    public MutableLiveData<String> artist = new MutableLiveData<>();

}
