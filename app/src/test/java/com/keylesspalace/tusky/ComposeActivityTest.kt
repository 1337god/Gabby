/* Copyright 2018 charlag
 *
 * This file is a part of Gabby.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Gabby is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Gabby; if not,
 * see <http://www.gnu.org/licenses>. */


package com.gab.gabby

import android.text.SpannedString
import android.widget.EditText
import com.gab.gabby.db.AccountEntity
import com.gab.gabby.db.AccountManager
import com.gab.gabby.db.AppDatabase
import com.gab.gabby.db.InstanceDao
import com.gab.gabby.entity.Account
import com.gab.gabby.entity.Emoji
import com.gab.gabby.entity.Instance
import com.gab.gabby.network.MastodonApi
import com.gab.gabby.util.ThemeUtils
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.Robolectric
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Created by charlag on 3/7/18.
 */

@Config(application = FakeGabbyApplication::class)
@RunWith(AndroidJUnit4::class)
class ComposeActivityTest {

    private lateinit var activity: ComposeActivity
    private lateinit var accountManagerMock: AccountManager
    private lateinit var apiMock: MastodonApi
    private lateinit var themeUtilsMock: ThemeUtils

    private val account = AccountEntity(
            id = 1,
            domain = "example.token",
            accessToken = "token",
            isActive = true,
            accountId = "1",
            username = "username",
            displayName = "Display Name",
            profilePictureUrl = "",
            notificationsEnabled = true,
            notificationsMentioned = true,
            notificationsFollowed = true,
            notificationsReblogged = true,
            notificationsFavorited = true,
            notificationSound = true,
            notificationVibration = true,
            notificationLight = true
    )
    var instanceResponseCallback: ((Call<Instance>?, Callback<Instance>?)->Unit)? = null

    @Before
    fun setupActivity() {
        val controller = Robolectric.buildActivity(ComposeActivity::class.java)
        activity = controller.get()

        accountManagerMock = Mockito.mock(AccountManager::class.java)

        apiMock = Mockito.mock(MastodonApi::class.java)
        `when`(apiMock.customEmojis).thenReturn(object: Call<List<Emoji>> {
            override fun isExecuted(): Boolean {
                return false
            }
            override fun clone(): Call<List<Emoji>> {
                throw Error("not implemented")
            }
            override fun isCanceled(): Boolean {
                throw Error("not implemented")
            }
            override fun cancel() {
                throw Error("not implemented")
            }
            override fun execute(): Response<List<Emoji>> {
                throw Error("not implemented")
            }
            override fun request(): Request {
                throw Error("not implemented")
            }

            override fun enqueue(callback: Callback<List<Emoji>>?) {}
        })
        `when`(apiMock.instance).thenReturn(object: Call<Instance> {
            override fun isExecuted(): Boolean {
                return false
            }
            override fun clone(): Call<Instance> {
                throw Error("not implemented")
            }
            override fun isCanceled(): Boolean {
                throw Error("not implemented")
            }
            override fun cancel() {
                throw Error("not implemented")
            }
            override fun execute(): Response<Instance> {
                throw Error("not implemented")
            }
            override fun request(): Request {
                throw Error("not implemented")
            }

            override fun enqueue(callback: Callback<Instance>?) {
                instanceResponseCallback?.invoke(this, callback)
            }
        })

        val instanceDaoMock = mock(InstanceDao::class.java)
        val dbMock = mock(AppDatabase::class.java)
        `when`(dbMock.instanceDao()).thenReturn(instanceDaoMock)

        themeUtilsMock = Mockito.mock(ThemeUtils::class.java)

        activity.mastodonApi = apiMock
        activity.accountManager = accountManagerMock
        activity.database = dbMock
        activity.themeUtils = themeUtilsMock

        `when`(accountManagerMock.activeAccount).thenReturn(account)


        controller.create().start()
    }

    @Test
    fun whenCloseButtonPressedAndEmpty_finish() {
        clickUp()
        assertTrue(activity.isFinishing)
    }

    @Test
    fun whenCloseButtonPressedNotEmpty_notFinish() {
        insertSomeTextInContent()
        clickUp()
        assertFalse(activity.isFinishing)
        // We would like to check for dialog but Robolectric doesn't work with AppCompat v7 yet
    }

    @Test
    fun whenBackButtonPressedAndEmpty_finish() {
        clickBack()
        assertTrue(activity.isFinishing)
    }

    @Test
    fun whenBackButtonPressedNotEmpty_notFinish() {
        insertSomeTextInContent()
        clickBack()
        assertFalse(activity.isFinishing)
        // We would like to check for dialog but Robolectric doesn't work with AppCompat v7 yet
    }

    @Test
    fun whenMaximumPostCharsIsNull_defaultLimitIsUsed() {
        instanceResponseCallback = getSuccessResponseCallbackWithMaximumPostCharacters(null)
        setupActivity()
        assertEquals(ComposeActivity.STATUS_CHARACTER_LIMIT, activity.maximumPostCharacters)
    }

    @Test
    fun whenMaximumPostCharsIsPopulated_customLimitIsUsed() {
        val customMaximum = 1000
        instanceResponseCallback = getSuccessResponseCallbackWithMaximumPostCharacters(customMaximum)
        setupActivity()
        assertEquals(customMaximum, activity.maximumPostCharacters)
    }

    @Test
    fun whenInitialInstanceRequestFails_defaultValueIsUsed() {
        instanceResponseCallback = {
            call: Call<Instance>?, callback: Callback<Instance>? ->
            if (call != null) {
                callback?.onResponse(call, Response.error(400, ResponseBody.create(null, "")))
            }
        }
        setupActivity()
        assertEquals(ComposeActivity.STATUS_CHARACTER_LIMIT, activity.maximumPostCharacters)
    }

    @Test
    fun whenTextContainsNoUrl_everyCharacterIsCounted() {
        val content = "This is test content please ignore thx "
        insertSomeTextInContent(content)
        Assert.assertEquals(activity.calculateTextLength(), content.length)
    }

    @Test
    fun whenTextContainsUrl_onlyEllipsizedURLIsCounted() {
        val url = "https://www.google.dk/search?biw=1920&bih=990&tbm=isch&sa=1&ei=bmDrWuOoKMv6kwWOkIaoDQ&q=indiana+jones+i+hate+snakes+animated&oq=indiana+jones+i+hate+snakes+animated&gs_l=psy-ab.3...54174.55443.0.55553.9.7.0.0.0.0.255.333.1j0j1.2.0....0...1c.1.64.psy-ab..7.0.0....0.40G-kcDkC6A#imgdii=PSp15hQjN1JqvM:&imgrc=H0hyE2JW5wrpBM:"
        val additionalContent = "Check out this @image #search result: "
        insertSomeTextInContent(additionalContent + url)
        Assert.assertEquals(activity.calculateTextLength(), additionalContent.length + ComposeActivity.MAXIMUM_URL_LENGTH)
    }

    @Test
    fun whenTextContainsMultipleUrls_onlyEllipsizedURLIsCounted() {
        val shortUrl = "https://gabby.app"
        val url = "https://www.google.dk/search?biw=1920&bih=990&tbm=isch&sa=1&ei=bmDrWuOoKMv6kwWOkIaoDQ&q=indiana+jones+i+hate+snakes+animated&oq=indiana+jones+i+hate+snakes+animated&gs_l=psy-ab.3...54174.55443.0.55553.9.7.0.0.0.0.255.333.1j0j1.2.0....0...1c.1.64.psy-ab..7.0.0....0.40G-kcDkC6A#imgdii=PSp15hQjN1JqvM:&imgrc=H0hyE2JW5wrpBM:"
        val additionalContent = " Check out this @image #search result: "
        insertSomeTextInContent(shortUrl + additionalContent + url)
        Assert.assertEquals(activity.calculateTextLength(), additionalContent.length + shortUrl.length + ComposeActivity.MAXIMUM_URL_LENGTH)
    }

    @Test
    fun whenTextContainsMultipleURLs_allURLsGetEllipsized() {
        val url = "https://www.google.dk/search?biw=1920&bih=990&tbm=isch&sa=1&ei=bmDrWuOoKMv6kwWOkIaoDQ&q=indiana+jones+i+hate+snakes+animated&oq=indiana+jones+i+hate+snakes+animated&gs_l=psy-ab.3...54174.55443.0.55553.9.7.0.0.0.0.255.333.1j0j1.2.0....0...1c.1.64.psy-ab..7.0.0....0.40G-kcDkC6A#imgdii=PSp15hQjN1JqvM:&imgrc=H0hyE2JW5wrpBM:"
        val additionalContent = " Check out this @image #search result: "
        insertSomeTextInContent(url + additionalContent + url)
        Assert.assertEquals(activity.calculateTextLength(), additionalContent.length + (ComposeActivity.MAXIMUM_URL_LENGTH * 2))
    }

    private fun clickUp() {
        val menuItem = RoboMenuItem(android.R.id.home)
        activity.onOptionsItemSelected(menuItem)
    }

    private fun clickBack() {
        activity.onBackPressed()
    }

    private fun insertSomeTextInContent(text: String? = null) {
        activity.findViewById<EditText>(R.id.composeEditField).setText(text ?: "Some text")
    }

    private fun getInstanceWithMaximumPostCharacters(maximumPostCharacters: Int?): Instance
    {
        return Instance(
                "https://example.token",
                "Example dot Token",
                "Example instance for testing",
                "admin@example.token",
                "2.6.3",
                HashMap(),
                null,
                null,
                listOf("en"),
                Account(
                        "1",
                        "admin",
                        "admin",
                        "admin",
                        SpannedString(""),
                        "https://example.token",
                        "",
                        "",
                        false,
                        0,
                        0,
                        0,
                        null,
                        false,
                        emptyList(),
                        emptyList()
                ),
                maximumPostCharacters
        )
    }

    private fun getSuccessResponseCallbackWithMaximumPostCharacters(maximumPostCharacters: Int?): (Call<Instance>?, Callback<Instance>?) -> Unit
    {
        return {
            call: Call<Instance>?, callback: Callback<Instance>? ->
            if (call != null) {
                callback?.onResponse(call, Response.success(getInstanceWithMaximumPostCharacters(maximumPostCharacters)))
            }
        }
    }
}