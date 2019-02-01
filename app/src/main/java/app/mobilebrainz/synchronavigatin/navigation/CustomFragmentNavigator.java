package app.mobilebrainz.synchronavigatin.navigation;

import android.annotation.SuppressLint;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;
import app.mobilebrainz.synchronavigatin.R;


@Navigator.Name("frame")
public class CustomFragmentNavigator extends Navigator<CustomFragmentNavigator.Destination> {

    private static final String TAG = "CustomFragmentNavigator";
    private static final String KEY_BACK_STACK_IDS = "CustomFragmentNavigator.KEY_BACK_STACK_IDS";
    private static final String KEY_FRAGMENT_QUEUE_NAMES = "CustomFragmentNavigator.KEY_FRAGMENT_QUEUE_NAMES";

    private static final int FRAGMENT_QUEUE_MAX = 10;

    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private final int mContainerId;
    private ArrayDeque<Integer> mBackStack = new ArrayDeque<>();
    private boolean mIsPendingBackStackOperation = false;

    private ArrayDeque<String> fragmentQueue = new ArrayDeque<>();
    private int fragmentQueueLimit = FRAGMENT_QUEUE_MAX;

    /*
    fragmentQueueLimit будет задавать стратегию замены фрагментов:
      - fragmentQueueLimit > 0 : attach-detach - с сохранением состояний фрагментов в фрагмент-менеджере, что может
        привести к значительному потреблению памяти при большом числе fragmentQueueLimit.
        Кроме того будет недоступна передача данных в дестинации через аргументы, потому что
        фрагменты не будут пересоздаваться. Все данные придётся передавать только через вью-модели,
        а обновление фрагментов производить по евентам из вью-моделей активности.

        Пока фрагмент находится в бек-стеке фрагмент-менеджера, фрагмент не удаляется, даже если он вытолкнут из fragmentQueue
        и задан на удаление из фрагмент-менеджера (fragmentTransaction.remove(popFragment)). Он только помещается в очередь на удаление
        и будет удалён из фрагмент-менеджера, когда очистится бек-стек, т.е. когда пройдёт обратная навигация через удаляемый фрагмент.
        И лишь после этого будет выполнена команда fragmentTransaction.remove(popFragment). После следующей навигации фрагмент будет пересоздан,
        а не аттачен из бек-стека. Поэтому особого смысла в fragmentQueueLimit нет без синхронизации его с бек-стеком. Надо как-то принудительно
        удалять фрагмент одновременно и из fragmentQueue и из бек-стека фрагмент-менеджера, тогда бек-стек ограничится размером fragmentQueueLimit, что есть плохо.
        С другой стороны это ограничит потребление памяти на бесконтрольно разбухающий бек-стек. Одновременно ограничить бек-стек и fragmentQueueLimit
        наврят ли получится, потому что первый - стек, а второй - очередь. Ими можно управлять только по отдельности. Напр. задавать размер бек-стека и
        размер фрагмент-очереди. Причём второй должен быть небольшим 4-5(размер боттома), максимум 6-8, потому что в основном фрагменты будут храниться в бек-стеке
        и только когда будут обратные вызовы, тогда на помощь будет приходить фрагмент-очередь.

        !!! Во время перехода назад верхней бек-кнопкой после достижения последнего возврата (бекстек==0)
        должен открываться дровер!!!

        В принципе можно не создавать фрагмент-очереди при аттаче, а все фрагменты держать в фрагмент-менеджере. Потому что размер бек-стека и сохрвнённых
        фрагментов = макимуму фрагментов приложения. При реплейсе бэкстек будет расти пока позволит память, потому что там могут создаваться множество
        экземпляров одного и того же фрагмента. Причём при использовании вью-модели с обсервом данных, все эти экземпляры одного фрагмента будут иметь
        одинаковые данные, т.е. он будут клонами, что выглядит абсурдом. Дефолтная система расчитана на старый асинхронный принцип построения
        приложения, когда данные фрагмента загружаются единожды при старте и не меяются. Современные приложения строятся на синхронном принципе -
        когда любые изменения данных отражаются во всём приложении в любое время и в этом случае реплейс с бекстеками выглядят архаично.

        - fragmentQueueLimit == 0 : replace - уничтожение старого фрагмента и создание нового с нуля.
        Экономия памяти, доступны аргументы в дестинации. Низкая производительность по причине постоянного
        пересоздания фрагментов при каждой навигации.

        !!! Ограничить бек-стек очень трудоёмко и не факт, что нужно это делать. Поэтому надо оставить только ограчничение
        на фрагмент-очередь и ввести максимум на фрагмент-очередь.

        Дефолтная реализация:
        - прямая навигация - создание фрагмента с нуля и сохранение в бек-стеке фрагмент-менеджера
        - обратная навигация - вызов фрагмента из бек-стека фрагмент-менеджера и аттачинг его без пересоздания.

     */
    private final FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {

                @SuppressLint("RestrictedApi")
                @Override
                public void onBackStackChanged() {
                    // If we have pending operations made by us then consume this change, otherwise
                    // detect a pop in the back stack to dispatch callback.
                    if (mIsPendingBackStackOperation) {
                        mIsPendingBackStackOperation = !isBackStackEqual();
                        return;
                    }

                    // The initial Fragment won't be on the back stack, so the
                    // real count of destinations is the back stack entry count + 1
                    int newCount = mFragmentManager.getBackStackEntryCount() + 1;
                    Log.i(TAG, "onBackStackChanged: ");
                    if (newCount < mBackStack.size()) {
                        // Handle cases where the user hit the system back button
                        while (mBackStack.size() > newCount) {
                            mBackStack.removeLast();
                        }
                        dispatchOnNavigatorBackPress();
                    }
                }
            };

    public CustomFragmentNavigator(@NonNull Context context, @NonNull FragmentManager manager, int containerId) {
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
    }

    public void setFragmentQueueLimit(int fragmentQueueLimit) {
        this.fragmentQueueLimit = fragmentQueueLimit;
    }

    @NonNull
    @Override
    public CustomFragmentNavigator.Destination createDestination() {
        return new CustomFragmentNavigator.Destination(this);
    }

    @Override
    protected void onBackPressAdded() {
        mFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    @Override
    protected void onBackPressRemoved() {
        mFragmentManager.removeOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    @Override
    public boolean popBackStack() {
        if (mBackStack.isEmpty() || mFragmentManager.isStateSaved()) {
            return false;
        }
        boolean popped = false;
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack(Integer.toString(mBackStack.peekLast()), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mIsPendingBackStackOperation = true;
            popped = true;
        }
        mBackStack.removeLast();
        return popped;
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
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
                fragmentTransaction.detach(currentFragment);
            }
            if (fragment != null) {
                fragmentTransaction.attach(fragment);
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
                    Fragment popFragment = mFragmentManager.findFragmentByTag(fragmentQueue.pop());
                    if (popFragment != null) {
                        // поставить в очередь на удаление из фрагмент-менеджера.
                        // будет удалён только после удаления этого фрагмента из бекстека.
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

        final boolean initialNavigation = mBackStack.isEmpty();
        // TODO Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId;

        boolean isAdded;
        if (initialNavigation) {
            isAdded = true;
        } else if (isSingleTopReplacement) {
            // Single Top means we only want one instance on the back stack
            if (mBackStack.size() > 1) {
                // If the Fragment to be replaced is on the FragmentManager's
                // back stack, a simple replace() isn't enough so we
                // remove it from the back stack and put our replacement
                // on the back stack in its place
                mFragmentManager.popBackStack();
                fragmentTransaction.addToBackStack(Integer.toString(destId));
                mIsPendingBackStackOperation = true;
            }
            isAdded = false;
        } else {
            fragmentTransaction.addToBackStack(Integer.toString(destId));
            mIsPendingBackStackOperation = true;
            isAdded = true;
        }
        if (navigatorExtras instanceof CustomFragmentNavigator.Extras) {
            CustomFragmentNavigator.Extras extras = (CustomFragmentNavigator.Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                fragmentTransaction.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commit();
        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId);
            return destination;
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public Bundle onSaveState() {
        Bundle bundle = new Bundle();

        int[] backStack = new int[mBackStack.size()];
        int index = 0;
        for (Integer id : mBackStack) {
            backStack[index++] = id;
        }
        bundle.putIntArray(KEY_BACK_STACK_IDS, backStack);

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
            int[] backStack = savedState.getIntArray(KEY_BACK_STACK_IDS);
            if (backStack != null) {
                mBackStack.clear();
                for (int destId : backStack) {
                    mBackStack.add(destId);
                }
            }

            String[] fragQueue = savedState.getStringArray(KEY_BACK_STACK_IDS);
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

    private boolean isBackStackEqual() {
        int fragmentBackStackCount = mFragmentManager.getBackStackEntryCount();
        // Initial fragment won't be on the FragmentManager's back stack so +1 its count.
        if (mBackStack.size() != fragmentBackStackCount + 1) {
            return false;
        }

        // From top to bottom verify destination ids match in both back stacks/
        Iterator<Integer> backStackIterator = mBackStack.descendingIterator();
        int fragmentBackStackIndex = fragmentBackStackCount - 1;
        while (backStackIterator.hasNext() && fragmentBackStackIndex >= 0) {
            int destId = backStackIterator.next();
            try {
                int fragmentDestId = Integer.valueOf(mFragmentManager
                        .getBackStackEntryAt(fragmentBackStackIndex--)
                        .getName());
                if (destId != fragmentDestId) {
                    return false;
                }
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid back stack entry on the "
                        + "CustomNavHostFragment's back stack - use getChildFragmentManager() "
                        + "if you need to do custom FragmentTransactions from within "
                        + "Fragments created via your navigation graph.");
            }
        }
        return true;
    }

    @NavDestination.ClassType(Fragment.class)
    public static class Destination extends NavDestination {

        private String mClassName;

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         */
        public Destination(@NonNull NavigatorProvider navigatorProvider) {
            this(navigatorProvider.getNavigator(CustomFragmentNavigator.class));
        }

        /**
         * Construct a new fragment destination. This destination is not valid until you set the
         * Fragment via {@link #setClassName(String)}.
         *
         * @param fragmentNavigator The {@link FragmentNavigator} which this destination
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
             * @param name The transitionName of the View in the Fragment being navigated to that
             *             should be matched to the shared element.
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
