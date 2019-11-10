package com.udacity.astroapp.fragments;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ObservatoryFragmentTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private MainActivity mainActivity = null;

    @Before
    public void setUp() throws Exception {
        mainActivity = mainActivityActivityTestRule.getActivity();
        AndroidTestHelper.chooseDrawerAction(R.id.nav_observatories);
        onView(withId(R.id.observatory_list_recycler_view)).perform(RecyclerViewChildActions.Companion.actionOnChild(
                click(), R.id.observatory_list_item_button));
    }

    @Test
    public void observatoryListItemIsDisplayed() {
        onView(withId(R.id.observatory_name)).check(matches(isDisplayed()));
        onView(withId(R.id.observatory_address)).check(matches(isDisplayed()));
        onView(withId(R.id.map_fragment_container)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() throws Exception {
        mainActivity = null;
    }
}