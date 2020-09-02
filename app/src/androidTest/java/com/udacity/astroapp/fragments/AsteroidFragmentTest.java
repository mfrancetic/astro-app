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
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AsteroidFragmentTest {

    @Rule
    public final ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private MainActivity mainActivity = null;

    @Before
    public void setUp() {
        mainActivity = mainActivityActivityTestRule.getActivity();
        AndroidTestHelper.chooseDrawerAction(R.id.nav_asteroids);
    }

    @Test
    public void asteroidRecyclerViewIsDisplayed() {
        onView(withId(R.id.asteroid_recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testAsteroidReadMoreButton() {
     onView(withId(R.id.asteroid_recycler_view)).perform(RecyclerViewChildActions.Companion.actionOnChild(
             click(), R.id.asteroid_read_more_button));
        Intent readMoreIntent = new Intent(Intent.ACTION_VIEW);
        assertNotNull(readMoreIntent);
    }

    @After
    public void tearDown() {
        mainActivity = null;
    }
}