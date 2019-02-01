package app.mobilebrainz.synchronavigatin.menu;

import java.util.ArrayList;
import java.util.List;

import app.mobilebrainz.synchronavigatin.R;


public class MenuStorage {

    private final List<DestinationMenu> destinationMenus = new ArrayList<>();

    private final static MenuStorage instance = new MenuStorage();

    public static MenuStorage getInstance() {
        return instance;
    }

    public final DestinationMenu getDestinationMenu(int destinationId) {
        for (DestinationMenu destinationMenu : destinationMenus) {
            if (destinationMenu.getDestinationId() == destinationId) {
                return destinationMenu;
            }
        }
        return null;
    }

    private MenuStorage() {
        initDestinationMenus();
    }

    private void addMenu(int destinationId, int bottomMenuId, int drawerMenuId, int optionsMenuId) {
        destinationMenus.add(new DestinationMenu(destinationId, bottomMenuId, drawerMenuId, optionsMenuId));
    }

    private void initDestinationMenus() {
        addMenu(R.id.searchFragment, R.menu.main_bottom_menu, R.menu.main_drawer_menu, R.menu.main_options_menu);
        addMenu(R.id.barcodeFragment, R.menu.main_bottom_menu, R.menu.main_drawer_menu, R.menu.main_options_menu);
        addMenu(R.id.aboutFragment, R.menu.main_bottom_menu, R.menu.main_drawer_menu, R.menu.main_options_menu);
        addMenu(R.id.feedbackFragment, R.menu.main_bottom_menu, R.menu.main_drawer_menu, R.menu.main_options_menu);
        addMenu(R.id.settingsFragment, R.menu.main_bottom_menu, R.menu.main_drawer_menu, R.menu.main_options_menu);

        addMenu(R.id.userProfilePagerFragment, R.menu.user_bottom_menu, R.menu.main_drawer_menu, R.menu.user_options_menu);
        addMenu(R.id.userCollectionsFragment, R.menu.user_bottom_menu, R.menu.main_drawer_menu, R.menu.user_options_menu);
        addMenu(R.id.userRatingsFragment, R.menu.user_bottom_menu, R.menu.main_drawer_menu, R.menu.user_options_menu);

        addMenu(R.id.artistReleasesFragment, R.menu.artist_bottom_menu, R.menu.artist_drawer_menu, R.menu.artist_options_menu);
        addMenu(R.id.artistRatingsFragment, R.menu.artist_bottom_menu, R.menu.artist_drawer_menu, R.menu.artist_options_menu);
        addMenu(R.id.artistTagsFragment, R.menu.artist_bottom_menu, R.menu.artist_drawer_menu, R.menu.artist_options_menu);
    }

}
