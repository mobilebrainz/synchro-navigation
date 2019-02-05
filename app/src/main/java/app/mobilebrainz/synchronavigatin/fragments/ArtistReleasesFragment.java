package app.mobilebrainz.synchronavigatin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.navigation.BundleViewModel;
import app.mobilebrainz.synchronavigatin.navigation.UpdatableFragmentInterface;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistAVM;
import app.mobilebrainz.synchronavigatin.viewmodels.ArtistReleasesVM;


public class ArtistReleasesFragment extends BaseFragment implements UpdatableFragmentInterface {

    public ArtistReleasesFragment() {
        //Log.i(TAG, "ArtistReleasesFragment: ");
    }

    private static final String TAG = "ArtistReleasesF";

    public static final String KEY_ARTIST_NAME = "ArtistReleasesFragment.KEY_ARTIST_NAME";

    private ArtistReleasesVM viewModel;
    private ArtistAVM artistAVM;
    private String artistName;

    // перенести в UpdatableFragment
    private BundleViewModel bundleViewModel;

    private TextView artistView;

    public static ArtistReleasesFragment newInstance() {
        return new ArtistReleasesFragment();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflate(R.layout.artist_releases_fragment, container);
        artistView = view.findViewById(R.id.artistView);
        Log.i(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i(TAG, "onActivityCreated: ");

        // перенести в UpdatableFragment
        // при первоначальном создании фрагмента
        if (bundleViewModel == null) {
            update(getArguments());
            bundleViewModel = getViewModel(BundleViewModel.class);
            /*
            при последующих вызовах детачченного фрагмента с новыми входными параметрами
             */
            bundleViewModel.observe(this, this::update);
        }

        viewModel = getViewModel(ArtistReleasesVM.class);
        artistAVM = getActivityViewModel(ArtistAVM.class);
        artistAVM.artist.observe(this, artist -> {
            Bundle bundle = new Bundle();
            // инициализация параметров фрагмента:
            // ...
            //update(bundle);
        });
    }

    // перенести в UpdatableFragment
    @Override
    public BundleViewModel getBundleViewModel() {
        return bundleViewModel;
    }

    // перенести в UpdatableFragment как абстрактный
    @Override
    public void update(Bundle bundle) {
        if (bundle != null) {
            artistName = bundle.getString(KEY_ARTIST_NAME);
            artistView.setText(artistName);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //artistView.setText(artistName);
    }
}
