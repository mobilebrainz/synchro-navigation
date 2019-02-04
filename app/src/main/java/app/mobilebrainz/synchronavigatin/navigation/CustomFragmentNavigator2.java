package app.mobilebrainz.synchronavigatin.navigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.NavigatorProvider;
import app.mobilebrainz.synchronavigatin.R;


@Navigator.Name("frame")
public class CustomFragmentNavigator2 extends Navigator<CustomFragmentNavigator2.Destination> {

    private static final String TAG = "CustomFragmentNavigator";

    private static final String KEY_FRAGMENT_QUEUE_LIMIT = "CustomFragmentNavigator.KEY_FRAGMENT_QUEUE_LIMIT";
    private static final String KEY_BACK_STACK_NAMES = "CustomFragmentNavigator.KEY_BACK_STACK_NAMES";
    private static final String KEY_FRAGMENT_QUEUE_NAMES = "CustomFragmentNavigator.KEY_FRAGMENT_QUEUE_NAMES";
    private static final int FRAGMENT_QUEUE_MAX = 10;

    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private final int mContainerId;
    private ArrayDeque<String> fragmentQueue = new ArrayDeque<>();
    private ArrayDeque<String> backStack = new ArrayDeque<>();
    private int fragmentQueueLimit = FRAGMENT_QUEUE_MAX;

    public CustomFragmentNavigator2(@NonNull Context context, @NonNull FragmentManager manager, int containerId) {
        this.mContext = context;
        this.mFragmentManager = manager;
        this.mContainerId = containerId;
    }

    public void setFragmentQueueLimit(int fragmentQueueLimit) {
        this.fragmentQueueLimit = fragmentQueueLimit;
    }

    @NonNull
    @Override
    public CustomFragmentNavigator2.Destination createDestination() {
        return new CustomFragmentNavigator2.Destination(this);
    }

    @Override
    public boolean popBackStack() {
        if (backStack.size() < 2 || mFragmentManager.isStateSaved()) {
            return false;
        }
        String popname = backStack.pop();
        String nextname = backStack.peekFirst();

        FragmentTransaction transacion = mFragmentManager.beginTransaction();
        transacion.disallowAddToBackStack();

        Fragment popfragment = mFragmentManager.findFragmentByTag(popname);
        Fragment nextfragment = mFragmentManager.findFragmentByTag(nextname);
        Log.i(TAG, "popBackStack: ");
        if (popfragment != null && nextfragment != null) {
            transacion.detach(popfragment);
            transacion.attach(nextfragment);
            if (!backStack.contains(popname) && !fragmentQueue.contains(popname)) {
                transacion.remove(popfragment);
            }
            transacion.commitNow();
            return true;
        } else {
            transacion.commitNow();
            return false;
        }
    }

    private static String makeFragmentName(int viewId, long id) {
        return "frame:" + viewId + ":" + id;
    }

    @NonNull
    public Fragment instantiateFragment(@NonNull Context context, @SuppressWarnings("unused") @NonNull FragmentManager fragmentManager,
                                        @NonNull String className, @Nullable Bundle args) {
        return Fragment.instantiate(context, className, args);
    }

    @Nullable
    @Override
    public NavDestination navigate(@NonNull Destination destination, @Nullable Bundle args, @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
        if (mFragmentManager.isStateSaved()) {
            return null;
        }
        final FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.disallowAddToBackStack();

        int enterAnim = navOptions != null ? navOptions.getEnterAnim() : -1;
        int exitAnim = navOptions != null ? navOptions.getExitAnim() : -1;
        int popEnterAnim = navOptions != null ? navOptions.getPopEnterAnim() : -1;
        int popExitAnim = navOptions != null ? navOptions.getPopExitAnim() : -1;
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = enterAnim != -1 ? enterAnim : 0;
            exitAnim = exitAnim != -1 ? exitAnim : 0;
            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
            fragmentTransaction.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
        }

        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }

        final @IdRes int destId = destination.getId();
        Fragment fragment;
        final String name = makeFragmentName(mContainerId, destId);
        if (fragmentQueueLimit > 0) {
            fragment = mFragmentManager.findFragmentByTag(name);
            Fragment currentFragment = mFragmentManager.findFragmentById(R.id.navHostView);
            if (currentFragment != null) {
                if (fragment == currentFragment) {
                    fragmentTransaction.commitNow();
                    return null;
                }
                fragmentTransaction.detach(currentFragment);
            }
            if (fragment != null) {
                fragmentTransaction.attach(fragment);

                ////// todo: тестировать при изменении конфигурации, когда фрагменты уничтожаются
                if (fragment instanceof UpdateBundle && args != null) {
                    BundleViewModel bundleViewModel = ((UpdateBundle) fragment).getBundleViewModel();
                    if (bundleViewModel != null) {
                        bundleViewModel.setBundle(args);
                    }
                }
                //////

            } else {
                fragment = instantiateFragment(mContext, mFragmentManager, className, args);
                fragment.setArguments(args);
                fragmentTransaction.add(mContainerId, fragment, name);
            }
            if (fragmentQueue.contains(name)) {
                fragmentQueue.remove(name);
                fragmentQueue.add(name);
            } else {
                fragmentQueue.add(name);
                if (fragmentQueue.size() > fragmentQueueLimit) {
                    String popname = fragmentQueue.pop();
                    Fragment popFragment = mFragmentManager.findFragmentByTag(popname);
                    if (popFragment != null && !backStack.contains(popname)) {
                        fragmentTransaction.remove(popFragment);
                    }
                }
            }
        } else {
            fragment = instantiateFragment(mContext, mFragmentManager, className, args);
            fragment.setArguments(args);
            fragmentTransaction.replace(mContainerId, fragment, name);
            fragmentTransaction.setPrimaryNavigationFragment(fragment);
        }
        fragmentTransaction.setPrimaryNavigationFragment(fragment);

        if (navigatorExtras instanceof CustomFragmentNavigator2.Extras) {
            CustomFragmentNavigator2.Extras extras = (CustomFragmentNavigator2.Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                fragmentTransaction.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }

        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNow();

        // todo: тестировать: удалить, если есть в бек-стеке, а потом добавить в голову
        // нельзя удалять стартовый фрагмент из бек-стека, иначе невозможно будет сбросить весь бек-стек к стартовому???
        // но вызов стартвого меню сьрасывает весь бек-стек, поэтому можно?
        // каким-то образом перепутывает имена в фрагмент-менеджере, кроме того - а нужно ли?
        //backStack.remove(name);
        backStack.push(name);
        Log.i(TAG, "navigate: ");

        return destination;
    }

    @Override
    @Nullable
    public Bundle onSaveState() {
        Bundle bundle = new Bundle();

        bundle.putInt(KEY_FRAGMENT_QUEUE_LIMIT, fragmentQueueLimit);

        String[] bStack = new String[backStack.size()];
        int index = 0;
        for (String name : backStack) {
            bStack[index++] = name;
        }
        bundle.putStringArray(KEY_BACK_STACK_NAMES, bStack);

        String[] fragQueue = new String[fragmentQueue.size()];
        index = 0;
        for (String name : fragmentQueue) {
            fragQueue[index++] = name;
        }
        bundle.putStringArray(KEY_FRAGMENT_QUEUE_NAMES, fragQueue);

        return bundle;
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        if (savedState != null) {
            fragmentQueueLimit = savedState.getInt(KEY_FRAGMENT_QUEUE_LIMIT, FRAGMENT_QUEUE_MAX);

            String[] bStack = savedState.getStringArray(KEY_BACK_STACK_NAMES);
            if (bStack != null) {
                backStack.clear();
                for (String name : bStack) {
                    if (mFragmentManager.findFragmentByTag(name) != null) {
                        backStack.add(name);
                    }
                }
            }

            String[] fragQueue = savedState.getStringArray(KEY_FRAGMENT_QUEUE_NAMES);
            if (fragQueue != null) {
                fragmentQueue.clear();
                for (String name : fragQueue) {
                    if (mFragmentManager.findFragmentByTag(name) != null) {
                        fragmentQueue.add(name);
                    }
                }
            }
        }
    }

    @NavDestination.ClassType(Fragment.class)
    public static class Destination extends NavDestination {

        private String mClassName;

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         */
        public Destination(@NonNull NavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(CustomFragmentNavigator2.class));
        }

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param fragmentNavigator The {@link CustomFragmentNavigator2} which this destination
         *                          will be associated with. Generally retrieved via a
         *                          {@link NavigatorProvider#getNavigator(Class)} method.
         */
        public Destination(@NonNull Navigator<? extends Destination> fragmentNavigator) {
            super(fragmentNavigator);
        }

        @CallSuper
        @Override
        public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs) {
            super.onInflate(context, attrs);
            TypedArray a = context.getResources().obtainAttributes(attrs,
                    R.styleable.CustomFragmentNavigator);
            String className = a.getString(R.styleable.CustomFragmentNavigator_android_name);
            if (className != null) {
                setClassName(className);
            }
            a.recycle();
        }

        /**
         * Set the Fragment class name associated with this destination
         *
         * @param className The class name of the Fragment to show when you navigate to this
         *                  destination
         * @return this {@link Destination}
         * @see #instantiateFragment(Context, FragmentManager, String, Bundle)
         */
        @NonNull
        public final Destination setClassName(@NonNull String className) {
            mClassName = className;
            return this;
        }

        /**
         * Gets the Fragment's class name associated with this destination
         *
         * @throws IllegalStateException when no Fragment class was set.
         * @see #instantiateFragment(Context, FragmentManager, String, Bundle)
         */
        @NonNull
        public final String getClassName() {
            if (mClassName == null) {
                throw new IllegalStateException("Fragment class was not set");
            }
            return mClassName;
        }
    }

    /**
     * Extras that can be passed to FragmentNavigator to enable Fragment specific behavior
     */
    public static final class Extras implements Navigator.Extras {
        private final LinkedHashMap<View, String> mSharedElements = new LinkedHashMap<>();

        Extras(Map<View, String> sharedElements) {
            mSharedElements.putAll(sharedElements);
        }

        /**
         * Gets the map of shared elements associated with these Extras. The returned map
         * is an {@link Collections#unmodifiableMap(Map) unmodifiable} copy of the underlying
         * map and should be treated as immutable.
         */
        @NonNull
        public Map<View, String> getSharedElements() {
            return Collections.unmodifiableMap(mSharedElements);
        }

        /**
         * Builder for constructing new {@link Extras} instances. The resulting instances are
         * immutable.
         */
        public static final class Builder {
            private final LinkedHashMap<View, String> mSharedElements = new LinkedHashMap<>();

            /**
             * Adds multiple shared elements for mapping Views in the current Fragment to
             * transitionNames in the Fragment being navigated to.
             *
             * @param sharedElements Shared element pairs to add
             * @return this {@link Builder}
             */
            @NonNull
            public Builder addSharedElements(@NonNull Map<View, String> sharedElements) {
                for (Map.Entry<View, String> sharedElement : sharedElements.entrySet()) {
                    View view = sharedElement.getKey();
                    String name = sharedElement.getValue();
                    if (view != null && name != null) {
                        addSharedElement(view, name);
                    }
                }
                return this;
            }

            /**
             * Maps the given View in the current Fragment to the given transition name in the
             * Fragment being navigated to.
             *
             * @param sharedElement A View in the current Fragment to match with a View in the
             *                      Fragment being navigated to.
             * @param name          The transitionName of the View in the Fragment being navigated to that
             *                      should be matched to the shared element.
             * @return this {@link Builder}
             * @see FragmentTransaction#addSharedElement(View, String)
             */
            @NonNull
            public Builder addSharedElement(@NonNull View sharedElement, @NonNull String name) {
                mSharedElements.put(sharedElement, name);
                return this;
            }

            /**
             * Constructs the final {@link Extras} instance.
             *
             * @return An immutable {@link Extras} instance.
             */
            @NonNull
            public Extras build() {
                return new Extras(mSharedElements);
            }
        }
    }

}
