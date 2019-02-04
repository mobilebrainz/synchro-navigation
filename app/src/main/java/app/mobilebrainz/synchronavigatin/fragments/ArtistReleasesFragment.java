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

    public ArtistReleasesFragment() {
        //Log.i(TAG, "ArtistReleasesFragment: ");
    }

    private static final String TAG = "ArtistReleasesF";

    public static final String KEY_ARTIST_NAME = "ArtistReleasesFragment.KEY_ARTIST_NAME";

    private ArtistReleasesVM viewModel;
    private ArtistAVM artistAVM;
    private BundleViewModel bundleViewModel;
    private String artistName;

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

        Log.i(TAG, "onActivityCreated: ");
        
        viewModel = getViewModel(ArtistReleasesVM.class);
        bundleViewModel = getViewModel(BundleViewModel.class);

        if (getArguments() != null) {
            // для последующего отслеживания изменения бандла
            bundleViewModel.setBundle(getArguments());
            artistName = getArguments().getString(KEY_ARTIST_NAME);
            show();
        }

        /*
        получить стартовые аргументы при навигации в этот фрагмент с входными параметрами
        и аттаче фрагмента
         */
        bundleViewModel.observe(this, bundle -> {
            if (bundle != null) {
                //artistName = bundle.getString(KEY_ARTIST_NAME);
                //show();
            }
        });

        artistAVM = getActivityViewModel(ArtistAVM.class);
        artistAVM.artist.observe(this, artist -> {
            //artistView.setText(artist);
        });
    }

    private void show() {
        artistView.setText(artistName);
    }

    @Override
    public BundleViewModel getBundleViewModel() {
        return bundleViewModel;
    }
}
