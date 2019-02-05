package app.mobilebrainz.synchronavigatin.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewParent;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import app.mobilebrainz.synchronavigatin.R;
import app.mobilebrainz.synchronavigatin.menu.DestinationMenu;
import app.mobilebrainz.synchronavigatin.menu.MenuStorage;
import app.mobilebrainz.synchronavigatin.menu.UpNavigationStorage;
import app.mobilebrainz.synchronavigatin.navigation.CustomFragmentNavigator;
import app.mobilebrainz.synchronavigatin.navigation.NavigationUIExtension;
import app.mobilebrainz.synchronavigatin.viewmodels.UserProfilePagerAVM;

import static app.mobilebrainz.synchronavigatin.adapter.pager.UserProfilePagerAdapter.TAB_USERS_POS;


public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int FRAGMENT_QUEUE_LIMIT = 8;

    private static final String BOTTOM_NAV_ID = "MainActivity.BOTTOM_NAV_ID";
    private static final String DRAWER_NAV_ID = "MainActivity.DRAWER_NAV_ID";
    private static final String OPTIONS_NAV_ID = "MainActivity.OPTIONS_NAV_ID";

    private static final int BOTTOM_NAV_DEFAULT = R.menu.main_bottom_menu;
    private static final int DRAWER_NAV_DEFAULT = R.menu.main_drawer_menu;
    private static final int OPTIONS_NAV_DEFAULT = R.menu.main_options_menu;

    private int bottomNavId;
    private int drawerNavId;
    private int optionsNavId;
    private int newOptionsNavId = -1;

    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbarView);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            bottomNavId = savedInstanceState.getInt(BOTTOM_NAV_ID, BOTTOM_NAV_DEFAULT);
            drawerNavId = savedInstanceState.getInt(DRAWER_NAV_ID, DRAWER_NAV_DEFAULT);
            optionsNavId = savedInstanceState.getInt(OPTIONS_NAV_ID, OPTIONS_NAV_DEFAULT);
        } else {
            bottomNavId = BOTTOM_NAV_DEFAULT;
            drawerNavId = DRAWER_NAV_DEFAULT;
            optionsNavId = OPTIONS_NAV_DEFAULT;
        }

        navController = Navigation.findNavController(this, R.id.navHostView);
        CustomFragmentNavigator customFragmentNavigator = navController.getNavigatorProvider().getNavigator(CustomFragmentNavigator.class);
        customFragmentNavigator.setFragmentQueueLimit(FRAGMENT_QUEUE_LIMIT);

        drawer = findViewById(R.id.drawerView);
        navigationView = findViewById(R.id.drawerNavView);
        navigationView.inflateMenu(drawerNavId);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        //NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.inflateMenu(bottomNavId);
        //NavigationUI.setupWithNavController(bottomNavView, navController);
        bottomNavView.setOnNavigationItemSelectedListener(initOnBottomNavigationListener());

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            DestinationMenu destinationMenu = MenuStorage.getInstance().getDestinationMenu(destination.getId());
            if (destinationMenu != null) {
                inflateBottomMenu(destinationMenu);
                inflateDrawerMenu(destinationMenu);
                newOptionsNavId = destinationMenu.getOptionsMenuId();
            }
        });

        // убрать, если будет нормально работать app:layout_behavior="@string/hide_bottom_view_on_scroll_behavior" в xml
        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavView.getLayoutParams();
        //layoutParams.setBehavior(new BottomNavigationViewBehavior());

        // в addOnDestinationChangedListener создать кастомный setupActionBarWithNavController:
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setSubtitle("Subtitle");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BOTTOM_NAV_ID, bottomNavId);
        outState.putInt(DRAWER_NAV_ID, drawerNavId);
        outState.putInt(OPTIONS_NAV_ID, optionsNavId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bottomNavId = savedInstanceState.getInt(BOTTOM_NAV_ID, BOTTOM_NAV_DEFAULT);
        drawerNavId = savedInstanceState.getInt(DRAWER_NAV_ID, DRAWER_NAV_DEFAULT);
        optionsNavId = savedInstanceState.getInt(OPTIONS_NAV_ID, OPTIONS_NAV_DEFAULT);
    }

    private void inflateBottomMenu(@NonNull DestinationMenu destinationMenu) {
        int bottomMenuId = destinationMenu.getBottomMenuId();
        if (bottomMenuId > 0) {
            if (bottomNavId != bottomMenuId) {
                bottomNavView.getMenu().clear();
                bottomNavView.inflateMenu(bottomMenuId);
                bottomNavId = bottomMenuId;
            }
            MenuItem bottomMenuItem = bottomNavView.getMenu().findItem(destinationMenu.getDestinationId());
            if (bottomMenuItem != null && !bottomMenuItem.isChecked()) {
                bottomMenuItem.setChecked(true);
            }
        }
    }

    private void inflateDrawerMenu(@NonNull DestinationMenu destinationMenu) {
        int drawerMenuId = destinationMenu.getDrawerMenuId();
        if (drawerMenuId > 0) {
            if (drawerNavId != drawerMenuId) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(drawerMenuId);
                drawerNavId = drawerMenuId;
            }
            Menu drawerMenu = navigationView.getMenu();
            MenuItem drawerMenuItem = drawerMenu.findItem(destinationMenu.getDestinationId());
            if (drawerMenuItem != null && !drawerMenuItem.isChecked()) {
                unCheckAllMenuItems(drawerMenu);
                drawerMenuItem.setChecked(true);
                scrollDrawerToPosition(drawerMenuItem.getOrder());
            }
        }
    }

    private void unCheckAllMenuItems(@NonNull final Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            if (item.hasSubMenu()) {
                unCheckAllMenuItems(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
    }

    private void scrollDrawerToPosition(int order) {
        final int limit = 4;
        if (order > limit) {
            RecyclerView recyclerView = (RecyclerView) navigationView.getChildAt(0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(order - limit, 0);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (newOptionsNavId > 0 && optionsNavId != newOptionsNavId) {
            invalidateOptionsMenu();
            menu.clear();
            getMenuInflater().inflate(newOptionsNavId, menu);
            optionsNavId = newOptionsNavId;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(optionsNavId, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        // сначала пытается навигироваться по иерархии, если не может, тогда по бек-стеку
        else if (!navigateUp() && !navController.popBackStack()) {
            super.onBackPressed();
        }
    }

    // навигация по иерархии, заданной в UpNavigationStorage
    private boolean navigateUp() {
        NavDestination currentDest = navController.getCurrentDestination();
        if (currentDest != null) {
            int currentDestId = currentDest.getId();
            int upNav = UpNavigationStorage.getInstance().get(currentDestId);
            return upNav > 0 && NavigationUIExtension.navigate(upNav, navController);
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean handled = true;
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                // сначала пытается навигироваться по иерархии, если не может - открыть дровер
                if (!navigateUp()) {
                    drawer.openDrawer(GravityCompat.START);
                }
                break;
            //для других menuItem, которые не входят в нав-граф:
            //case R.id....:
            // ...
            // break;
            default:
                handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
        }
        return handled;
    }

    //Custom NavigationUI.setupWithNavController(navigationView, navController);
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        boolean handled = true;
        switch (menuItem.getItemId()) {
            // navigate with params to other destination
            case R.id.usersTabFragment:
                // navigate to ViewPagerFragment with tabPos in ViewPagerFragmentActivityVM
                navController.navigate(R.id.userProfilePagerFragment);
                UserProfilePagerAVM userProfilePagerAVM = getViewModel(UserProfilePagerAVM.class);
                userProfilePagerAVM.tabPosition.setValue(TAB_USERS_POS);
                break;

            default:
                handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
        }
        if (handled) {
            ViewParent parent = navigationView.getParent();
            if (parent instanceof DrawerLayout) {
                ((DrawerLayout) parent).closeDrawer(navigationView);
            } else {
                BottomSheetBehavior bottomSheetBehavior = NavigationUIExtension.findBottomSheetBehavior(navigationView);
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        }
        return handled;
    }

    // Custom NavigationUI.setupWithNavController(bottomNavView, navController);
    protected BottomNavigationView.OnNavigationItemSelectedListener initOnBottomNavigationListener() {
        return menuItem -> {
            boolean handled = true;
            switch (menuItem.getItemId()) {
                // navigate with params to other destination
                //case R.id.usersFragment:
                //navController.navigate(R.id.userProfilePagerFragment);
                //UserProfilePagerAVM userProfilePagerAVM = getViewModel(UserProfilePagerAVM.class);
                //userProfilePagerAVM.tabPosition.setValue(TAB_USERS_POS);
                //break;
                default:
                    handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
            return handled;
        };
    }
}
