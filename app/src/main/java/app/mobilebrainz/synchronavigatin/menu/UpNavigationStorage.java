package app.mobilebrainz.synchronavigatin.menu;


import android.util.SparseIntArray;

import app.mobilebrainz.synchronavigatin.R;


public class UpNavigationStorage {

    private final SparseIntArray backNavigations = new SparseIntArray();

    private final static UpNavigationStorage instance = new UpNavigationStorage();

    public static UpNavigationStorage getInstance() {
        return instance;
    }

    public final int get(int destinationId) {
        return backNavigations.get(destinationId);
    }

    private UpNavigationStorage() {
        initBackNavigations();
    }

    private void put(int destinationId, int actionId) {
        backNavigations.put(destinationId, actionId);
    }

    private void initBackNavigations() {
        put(R.id.userProfilePagerFragment, R.id.action_userProfilePagerFragment_to_searchFragment);
        put(R.id.artistReleasesFragment, R.id.action_artistReleasesFragment_to_userProfilePagerFragment);
        put(R.id.artistRatingsFragment, R.id.action_artistRatingsFragment_to_artistReleasesFragment);
        put(R.id.artistTagsFragment, R.id.action_artistTagsFragment_to_artistReleasesFragment);
        put(R.id.userCollectionsFragment, R.id.action_userCollectionsFragment_to_userProfilePagerFragment);
        put(R.id.userRatingsFragment, R.id.action_userRatingsFragment_to_userProfilePagerFragment);
    }
}
