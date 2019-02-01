package app.mobilebrainz.synchronavigatin.navigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NavigationRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.Navigator;
import app.mobilebrainz.synchronavigatin.R;

/**
 * CustomNavHostFragment provides an area within your layout for self-contained navigation to occur.
 *
 * <p>CustomNavHostFragment is intended to be used as the content area within a layout resource
 * defining your app's chrome around it, e.g.:</p>
 *
 * <pre class="prettyprint">
 * &lt;android.support.v4.widget.DrawerLayout
 *        xmlns:android="http://schemas.android.com/apk/res/android"
 *        xmlns:app="http://schemas.android.com/apk/res-auto"
 *        android:layout_width="match_parent"
 *        android:layout_height="match_parent"&gt;
 *    &lt;fragment
 *            android:layout_width="match_parent"
 *            android:layout_height="match_parent"
 *            android:id="@+id/my_nav_host_fragment"
 *            android:name="app.mobilebrainz.synchronavigatin.navigation.CustomNavHostFragment"
 *            app:navGraph="@xml/nav_sample"
 *            app:defaultNavHost="true" /&gt;
 *    &lt;android.support.design.widget.NavigationView
 *            android:layout_width="wrap_content"
 *            android:layout_height="match_parent"
 *            android:layout_gravity="start"/&gt;
 * &lt;/android.support.v4.widget.DrawerLayout&gt;
 * </pre>
 *
 * <p>Each CustomNavHostFragment has a {@link NavController} that defines valid navigation within
 * the navigation host. This includes the {@link NavGraph navigation graph} as well as navigation
 * state such as current location and back stack that will be saved and restored along with the
 * CustomNavHostFragment itself.</p>
 *
 * <p>NavHostFragments register their navigation controller at the root of their view subtree
 * such that any descendant can obtain the controller instance through the {@link Navigation}
 * helper class's methods such as {@link Navigation#findNavController(View)}. View event listener
 * implementations such as {@link android.view.View.OnClickListener} within navigation destination
 * fragments can use these helpers to navigate based on user interaction without creating a tight
 * coupling to the navigation host.</p>
 */
public class CustomNavHostFragment extends Fragment implements NavHost {
    private static final String KEY_GRAPH_ID = "CustomNavHostFragment.KEY_GRAPH_ID";
    private static final String KEY_START_DESTINATION_ARGS = "CustomNavHostFragment.KEY_START_DESTINATION_ARGS";
    private static final String KEY_NAV_CONTROLLER_STATE = "CustomNavHostFragment.KEY_NAV_CONTROLLER_STATE";
    private static final String KEY_DEFAULT_NAV_HOST = "CustomNavHostFragment.KEY_DEFAULT_NAV_HOST";

    /**
     * Find a {@link NavController} given a local {@link Fragment}.
     *
     * <p>This method will locate the {@link NavController} associated with this Fragment,
     * looking first for a {@link CustomNavHostFragment} along the given Fragment's parent chain.
     * If a {@link NavController} is not found, this method will look for one along this
     * Fragment's {@link Fragment#getView() view hierarchy} as specified by
     * {@link Navigation#findNavController(View)}.</p>
     *
     * @param fragment the locally scoped Fragment for navigation
     * @return the locally scoped {@link NavController} for navigating from this {@link Fragment}
     * @throws IllegalStateException if the given Fragment does not correspond with a
     * {@link NavHost} or is not within a NavHost.
     */
    @NonNull
    public static NavController findNavController(@NonNull Fragment fragment) {
        Fragment findFragment = fragment;
        while (findFragment != null) {
            if (findFragment instanceof CustomNavHostFragment) {
                return ((CustomNavHostFragment) findFragment).getNavController();
            }
            Fragment primaryNavFragment = findFragment.requireFragmentManager()
                    .getPrimaryNavigationFragment();
            if (primaryNavFragment instanceof CustomNavHostFragment) {
                return ((CustomNavHostFragment) primaryNavFragment).getNavController();
            }
            findFragment = findFragment.getParentFragment();
        }

        // Try looking for one associated with the view instead, if applicable
        View view = fragment.getView();
        if (view != null) {
            return Navigation.findNavController(view);
        }
        throw new IllegalStateException("Fragment " + fragment + " does not have a NavController set");
    }

    private NavController mNavController;

    // State that will be saved and restored
    private int mGraphId;
    private boolean mDefaultNavHost;

    /**
     * Create a new CustomNavHostFragment instance with an inflated {@link NavGraph} resource.
     *
     * @param graphResId resource id of the navigation graph to inflate
     * @return a new CustomNavHostFragment instance
     */
    @NonNull
    public static CustomNavHostFragment create(@NavigationRes int graphResId) {
        return create(graphResId, null);
    }

    /**
     * Create a new CustomNavHostFragment instance with an inflated {@link NavGraph} resource.
     *
     * @param graphResId resource id of the navigation graph to inflate
     * @param startDestinationArgs arguments to send to the start destination of the graph
     * @return a new CustomNavHostFragment instance
     */
    @NonNull
    public static CustomNavHostFragment create(@NavigationRes int graphResId, @Nullable Bundle startDestinationArgs) {
        Bundle b = null;
        if (graphResId != 0) {
            b = new Bundle();
            b.putInt(KEY_GRAPH_ID, graphResId);
        }
        if (startDestinationArgs != null) {
            if (b == null) {
                b = new Bundle();
            }
            b.putBundle(KEY_START_DESTINATION_ARGS, startDestinationArgs);
        }

        final CustomNavHostFragment result = new CustomNavHostFragment();
        if (b != null) {
            result.setArguments(b);
        }
        return result;
    }

    /**
     * Returns the {@link NavController navigation controller} for this navigation host.
     * This method will return null until this host fragment's {@link #onCreate(Bundle)}
     * has been called and it has had an opportunity to restore from a previous instance state.
     *
     * @return this host's navigation controller
     * @throws IllegalStateException if called before {@link #onCreate(Bundle)}
     */
    @NonNull
    @Override
    public final NavController getNavController() {
        if (mNavController == null) {
            throw new IllegalStateException("NavController is not available before onCreate()");
        }
        return mNavController;
    }

    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // TODO This feature should probably be a first-class feature of the Fragment system,
        // but it can stay here until we can add the necessary attr resources to
        // the fragment lib.
        if (mDefaultNavHost) {
            requireFragmentManager().beginTransaction()
                    .setPrimaryNavigationFragment(this)
                    .commit();
        }
    }

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = requireContext();

        mNavController = new NavController(context);
        mNavController.getNavigatorProvider().addNavigator(createFragmentNavigator());

        Bundle navState = null;
        if (savedInstanceState != null) {
            navState = savedInstanceState.getBundle(KEY_NAV_CONTROLLER_STATE);
            if (savedInstanceState.getBoolean(KEY_DEFAULT_NAV_HOST, false)) {
                mDefaultNavHost = true;
                requireFragmentManager().beginTransaction()
                        .setPrimaryNavigationFragment(this)
                        .commit();
            }
        }

        if (navState != null) {
            // Navigation controller state overrides arguments
            mNavController.restoreState(navState);
        }
        if (mGraphId != 0) {
            // Set from onInflate()
            mNavController.setGraph(mGraphId);
        } else {
            // See if it was set by CustomNavHostFragment.create()
            final Bundle args = getArguments();
            final int graphId = args != null ? args.getInt(KEY_GRAPH_ID) : 0;
            final Bundle startDestinationArgs = args != null
                    ? args.getBundle(KEY_START_DESTINATION_ARGS)
                    : null;
            if (graphId != 0) {
                mNavController.setGraph(graphId, startDestinationArgs);
            }
        }
    }

    /**
     * Create the CustomFragmentNavigator that this CustomNavHostFragment will use. By default, this uses
     * CustomFragmentNavigator, which replaces the entire contents of the CustomNavHostFragment.
     * <p>
     * This is only called once in {@link #onCreate(Bundle)} and should not be called directly by
     * subclasses.
     * @return a new instance of a FragmentNavigator
     */
    @NonNull
    protected Navigator<? extends CustomFragmentNavigator.Destination> createFragmentNavigator() {
        return new CustomFragmentNavigator(requireContext(), getChildFragmentManager(), getId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(inflater.getContext());
        // When added via XML, this has no effect (since this FrameLayout is given the ID
        // automatically), but this ensures that the View exists as part of this Fragment's View
        // hierarchy in cases where the CustomNavHostFragment is added programmatically as is required
        // for child fragment transactions
        frameLayout.setId(getId());
        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!(view instanceof ViewGroup)) {
            throw new IllegalStateException("created host view " + view + " is not a ViewGroup");
        }
        // When added via XML, the parent is null and our view is the root of the CustomNavHostFragment
        // but when added programmatically, we need to set the NavController on the parent - i.e.,
        // the View that has the ID matching this CustomNavHostFragment.
        View rootView = view.getParent() != null ? (View) view.getParent() : view;
        Navigation.setViewNavController(rootView, mNavController);
    }

    @CallSuper
    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, @Nullable Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomNavHostFragment);
        final int graphId = a.getResourceId(R.styleable.CustomNavHostFragment_navGraph, 0);
        final boolean defaultHost = a.getBoolean(R.styleable.CustomNavHostFragment_defaultNavHost, false);

        if (graphId != 0) {
            mGraphId = graphId;
        }
        if (defaultHost) {
            mDefaultNavHost = true;
        }
        a.recycle();
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle navState = mNavController.saveState();
        if (navState != null) {
            outState.putBundle(KEY_NAV_CONTROLLER_STATE, navState);
        }
        if (mDefaultNavHost) {
            outState.putBoolean(KEY_DEFAULT_NAV_HOST, true);
        }
    }
}
