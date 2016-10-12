package com.shovelgrill.kancollebattery.ui;

import android.content.pm.ActivityInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.EditText;

import com.shovelgrill.kancollebattery.DownloadActivity;
import com.shovelgrill.kancollebattery.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

/**
 * Created by qwerty on 24. 8. 2016.
 */

@RunWith(AndroidJUnit4.class)
public class UiTest {

    @Rule
    public ActivityTestRule<DownloadActivity> activityTestRule = new ActivityTestRule<DownloadActivity>(DownloadActivity.class);


    @Test
    public void testCancelOfDialog() throws Exception{
        String progrees_message = InstrumentationRegistry.getTargetContext().getString(R.string.progress_message);

        onView(withId(R.id.action_download)).perform(click());
        onView(withText(R.string.string_refresh_ships_question)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).perform(click());
        onView(withText(R.string.string_refresh_ships_question)).check(doesNotExist());
        onView(withId(R.id.action_download)).perform(click());
        onView(withText(R.string.string_refresh_ships_question)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(R.string.string_refresh_ships_question)).check(doesNotExist());
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
        pressBack();
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
        pressBack();
        pressBack();
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
        pressBack();
        pressBack();
        pressBack();
        pressBack();
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
        activityTestRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
        activityTestRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(startsWith(progrees_message))).inRoot(isDialog()).check(matches(isDisplayed()));
    }


    @Test
    public void testSearchShimakaze() throws Exception{
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText("satsuki"), pressKey(KeyEvent.KEYCODE_ENTER));
        onView(allOf(withId(R.id.image_set_skin_name),withText("Satsuki"))).check(matches(isDisplayed()));
    }




}
