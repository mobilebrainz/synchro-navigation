package app.mobilebrainz.synchronavigatin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.viewmodels.UserProfileVM;


public class UserProfileFragment extends BaseFragment {

    private static final String TAG = "UserProfileFragment";

    private UserProfileVM viewModel;

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflate(R.layout.user_profile_fragment, container);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = getViewModel(UserProfileVM.class);
    }

}
