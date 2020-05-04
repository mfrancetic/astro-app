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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PhotoFragmentTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private MainActivity mainActivity = null;

    @Before
    public void setUp() throws Exception {
        mainActivity = mainActivityActivityTestRule.getActivity();
        mainActivityActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction();
    }

    @Test
    public void testFab() {
        onView(withId(R.id.fab)).perform(click());
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        assertNotNull(shareIntent);
    }

    @Test
    public void testPreviousButton() {
        assertNotNull(withId(R.id.photo_previous_button));
        onView(withId(R.id.photo_previous_button)).perform(click());
    }

    @Test
    public void testSourceTextView() {
        onView(withId(R.id.earth_photo_source_text_view)).check(matches(withText(mainActivity
        .getString(R.string.photo_video_source))));
    }

    @Test
    public void testPhoto() {
        assertNotNull(withId(R.id.earth_photo_frame_layout));
    }

    @Test
    public void testPhotoDescription() {
        assertNotNull(withId(R.id.photo_description_text_view));
    }

    @After
    public void tearDown() throws Exception {
        mainActivity = null;
    }
}