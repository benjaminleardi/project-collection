@file:Suppress("detekt:all")
@file:JvmName("Views")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import android.view.View
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.common.truth.Truth.assertWithMessage
import org.hamcrest.Matcher

fun countRecyclerView(expected: Int): ViewAssertion = ViewAssertion { v, noViewFoundException ->
    if (noViewFoundException != null) {
        throw noViewFoundException
    }
    val view = v as RecyclerView
    val adapter = view.adapter
    assertWithMessage("View adapter should not be null").that(adapter).isNotNull()
    assertWithMessage("Adapter should have $expected items")
        .that(adapter!!.itemCount)
        .isEqualTo(expected)
}

fun searchFor(query: String): ViewAction = searchFor(query, false)

fun searchFor(query: String, submit: Boolean): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isDisplayed()

    override fun getDescription(): String = if (submit) {
        "Set query to $query and submit"
    } else {
        "Set query to $query but don't submit"
    }

    override fun perform(uiController: UiController, view: View) {
        val searchView = view as SearchView
        searchView.setQuery(query, submit)
    }
}

fun isChecked(checked: Boolean): ViewAssertion = ViewAssertion { view, noViewFoundException ->
    assertWithMessage("Should have found view").that(noViewFoundException).isNull()
    assertWithMessage("View should be a ToggleButton")
        .that(view)
        .isInstanceOf(ToggleButton::class.java)

    val toggleButton = view as ToggleButton
    val message: String = if (checked) {
        "ToggleButton should be checked"
    } else {
        "ToggleButton should not be checked"
    }
    assertWithMessage(message).that(toggleButton.isChecked).isEqualTo(checked)
}

fun setChecked(checked: Boolean): ViewAction = actionWithAssertions(
    object : ViewAction {
        override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(ToggleButton::class.java)

        override fun getDescription(): String = "Custom view action to check or uncheck ToggleButton"

        override fun perform(uiController: UiController, view: View) {
            val toggleButton = view as ToggleButton
            toggleButton.isChecked = checked
        }
    },
)
