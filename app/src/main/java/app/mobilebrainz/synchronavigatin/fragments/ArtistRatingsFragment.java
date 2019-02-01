package app.mobilebrainz.synchronavigatin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistRatingsVM;


public class ArtistRatingsFragment extends BaseFragment {

    private ArtistRatingsVM viewModel;

    public static ArtistRatingsFragment newInstance() {
        return new ArtistRatingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflate(R.layout.artist_ratings_fragment, container);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = getViewModel(ArtistRatingsVM.class);
    }

}
