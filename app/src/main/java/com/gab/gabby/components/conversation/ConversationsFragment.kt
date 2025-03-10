/* Copyright 2019 Conny Duck
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

package com.gab.gabby.components.conversation

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gab.gabby.AccountActivity
import com.gab.gabby.R
import com.gab.gabby.ViewTagActivity
import com.gab.gabby.db.AppDatabase
import com.gab.gabby.di.Injectable
import com.gab.gabby.di.ViewModelFactory
import com.gab.gabby.fragment.SFragment
import com.gab.gabby.fragment.SearchFragment
import com.gab.gabby.interfaces.ReselectableFragment
import com.gab.gabby.interfaces.StatusActionListener
import com.gab.gabby.util.NetworkState
import com.gab.gabby.util.ThemeUtils
import com.gab.gabby.util.hide
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_timeline.*
import javax.inject.Inject

class ConversationsFragment : SFragment(), StatusActionListener, Injectable, ReselectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var db: AppDatabase

    private lateinit var viewModel: ConversationsViewModel

    private lateinit var adapter: ConversationAdapter

    private var layoutManager: LinearLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[ConversationsViewModel::class.java]

        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val preferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        val useAbsoluteTime = preferences.getBoolean("absoluteTimeView", false)

        val account = accountManager.activeAccount
        val mediaPreviewEnabled = account?.mediaPreviewEnabled ?: true


        adapter = ConversationAdapter(useAbsoluteTime, mediaPreviewEnabled, this, ::onTopLoaded, viewModel::retry)

        recyclerView.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        layoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        progressBar.hide()
        statusView.hide()

        initSwipeToRefresh()

        viewModel.conversations.observe(this, Observer<PagedList<ConversationEntity>> {
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })

        viewModel.load()

    }

    private fun initSwipeToRefresh() {
        viewModel.refreshState.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        })
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        swipeRefreshLayout.setColorSchemeResources(R.color.gabby_blue)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeUtils.getColor(swipeRefreshLayout.context, android.R.attr.colorBackground))
    }

    private fun onTopLoaded() {
        recyclerView.scrollToPosition(0)
    }

    override fun onReblog(reblog: Boolean, position: Int) {
        // its impossible to reblog private messages
    }

    override fun onFavourite(favourite: Boolean, position: Int) {
        viewModel.favourite(favourite, position)
    }

    override fun onMore(view: View, position: Int) {
        viewModel.conversations.value?.getOrNull(position)?.lastStatus?.let {
            more(it.toStatus(), view, position)
        }
    }

    override fun onViewMedia(position: Int, attachmentIndex: Int, view: View?) {
        viewModel.conversations.value?.getOrNull(position)?.lastStatus?.let {
            viewMedia(attachmentIndex, it.toStatus(), view)
        }
    }

    override fun onViewThread(position: Int) {
        viewModel.conversations.value?.getOrNull(position)?.lastStatus?.let {
            viewThread(it.toStatus())
        }
    }

    override fun onOpenReblog(position: Int) {
        // there are no reblogs in search results
    }

    override fun onExpandedChange(expanded: Boolean, position: Int) {
        viewModel.expandHiddenStatus(expanded, position)
    }

    override fun onContentHiddenChange(isShowing: Boolean, position: Int) {
        viewModel.showContent(isShowing, position)
    }

    override fun onLoadMore(position: Int) {
        // not using the old way of pagination
    }

    override fun onContentCollapsedChange(isCollapsed: Boolean, position: Int) {
        viewModel.collapseLongStatus(isCollapsed, position)
    }

    override fun onViewAccount(id: String) {
        val intent = AccountActivity.getIntent(requireContext(), id)
        startActivity(intent)
    }

    override fun onViewTag(tag: String) {
        val intent = Intent(context, ViewTagActivity::class.java)
        intent.putExtra("hashtag", tag)
        startActivity(intent)
    }

    override fun removeItem(position: Int) {
        viewModel.remove(position)
    }

    override fun onReply(position: Int) {
        viewModel.conversations.value?.getOrNull(position)?.lastStatus?.let {
            reply(it.toStatus())
        }
    }

    private fun jumpToTop() {
        if (isAdded) {
            layoutManager?.scrollToPosition(0)
            recyclerView.stopScroll()
        }
    }

    override fun onReselect() {
        jumpToTop()
    }

    override fun onVoteInPoll(position: Int, choices: MutableList<Int>) {
        viewModel.voteInPoll(position, choices)
    }

    companion object {
        fun newInstance() = ConversationsFragment()
    }
}
