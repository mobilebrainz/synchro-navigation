package app.mobilebrainz.synchronavigatin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.viewmodels.UserProfilePagerAVM;
import app.mobilebrainz.synchronavigatin.viewmodels.UsersViewModel;


public class UsersFragment extends BaseFragment {

    private UsersViewModel viewModel;
    private UserProfilePagerAVM aViewModel;

    public static UsersFragment newInstance() {
        return new UsersFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflate(R.layout.users_fragment, container);

        Button artistButton = view.findViewById(R.id.artistButton);
        artistButton.setOnClickListener(v -> {
            aViewModel.onArtist.setValue("Deep Purple");
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = getViewModel(UsersViewModel.class);
        aViewModel = getActivityViewModel(UserProfilePagerAVM.class);
    }

}
