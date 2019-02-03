package app.mobilebrainz.synchronavigatin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.navigation.BundleViewModel;
import app.mobilebrainz.synchronavigatin.navigation.UpdateBundle;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistAVM;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistReleasesVM;


public class ArtistReleasesFragment extends BaseFragment implements UpdateBundle {

    private static final String TAG = "ArtistReleasesF";

    private ArtistReleasesVM viewModel;
    private ArtistAVM artistAVM;
    private BundleViewModel bundleViewModel;

    private TextView artistView;

    public static ArtistReleasesFragment newInstance() {
        return new ArtistReleasesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflate(R.layout.artist_releases_fragment, container);
        artistView = view.findViewById(R.id.artistView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = getViewModel(ArtistReleasesVM.class);
        bundleViewModel = getViewModel(BundleViewModel.class);
        artistAVM = getActivityViewModel(ArtistAVM.class);
        bundleViewModel.observe(this, bundle -> {
            Log.i(TAG, "onActivityCreated: ");
        });

        artistAVM.artist.observe(this, artist -> {
            artistView.setText(artist);
        });
    }

    @Override
    public BundleViewModel getBundleViewModel() {
        return bundleViewModel;
    }
}
