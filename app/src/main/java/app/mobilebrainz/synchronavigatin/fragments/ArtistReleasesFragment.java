package app.mobilebrainz.synchronavigatin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistAVM;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistReleasesVM;


public class ArtistReleasesFragment extends BaseFragment {

    private ArtistReleasesVM viewModel;
    private ArtistAVM artistAVM;

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
        artistAVM = getActivityViewModel(ArtistAVM.class);
        artistAVM.artist.observe(this, artist -> {
            artistView.setText(artist);
        });
    }

}
