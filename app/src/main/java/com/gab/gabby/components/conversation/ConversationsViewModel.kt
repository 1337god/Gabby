package com.gab.gabby.components.conversation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.gab.gabby.db.AccountManager
import com.gab.gabby.db.AppDatabase
import com.gab.gabby.network.TimelineCases
import com.gab.gabby.util.Listing
import com.gab.gabby.util.NetworkState
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ConversationsViewModel @Inject constructor(
        private val repository: ConversationsRepository,
        private val timelineCases: TimelineCases,
        private val database: AppDatabase,
        private val accountManager: AccountManager
) : ViewModel() {

    private val repoResult = MutableLiveData<Listing<ConversationEntity>>()

    val conversations: LiveData<PagedList<ConversationEntity>> = Transformations.switchMap(repoResult) { it.pagedList }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(repoResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(repoResult) { it.refreshState }

    private val disposables = CompositeDisposable()

    fun load() {
        val accountId = accountManager.activeAccount?.id ?: return
        if (repoResult.value == null) {
            repository.refresh(accountId, false)
        }
        repoResult.value = repository.conversations(accountId)
    }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun retry() {
        repoResult.value?.retry?.invoke()
    }

    fun favourite(favourite: Boolean, position: Int) {
        conversations.value?.getOrNull(position)?.let { conversation ->
            timelineCases.favourite(conversation.lastStatus.toStatus(), favourite)
                    .flatMap {
                        val newConversation = conversation.copy(
                                lastStatus = conversation.lastStatus.copy(favourited = favourite)
                        )
                        Single.fromCallable {
                            database.conversationDao().insert(newConversation)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .doOnError { t -> Log.w("ConversationViewModel", "Failed to favourite conversation", t) }
                    .subscribe()
                    .addTo(disposables)
        }

    }

    fun voteInPoll(position: Int, choices: MutableList<Int>) {
        conversations.value?.getOrNull(position)?.let { conversation ->
            timelineCases.voteInPoll(conversation.lastStatus.toStatus(), choices)
                    .flatMap { poll ->
                        val newConversation = conversation.copy(
                                lastStatus = conversation.lastStatus.copy(poll = poll)
                        )
                        Single.fromCallable {
                            database.conversationDao().insert(newConversation)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .doOnError { t -> Log.w("ConversationViewModel", "Failed to favourite conversation", t) }
                    .subscribe()
                    .addTo(disposables)
        }

    }

    fun expandHiddenStatus(expanded: Boolean, position: Int) {
        conversations.value?.getOrNull(position)?.let { conversation ->
            val newConversation = conversation.copy(
                    lastStatus = conversation.lastStatus.copy(expanded = expanded)
            )
            saveConversationToDb(newConversation)
        }
    }

    fun collapseLongStatus(collapsed: Boolean, position: Int) {
        conversations.value?.getOrNull(position)?.let { conversation ->
            val newConversation = conversation.copy(
                    lastStatus = conversation.lastStatus.copy(collapsed = collapsed)
            )
            saveConversationToDb(newConversation)
        }
    }

    fun showContent(showing: Boolean, position: Int) {
        conversations.value?.getOrNull(position)?.let { conversation ->
            val newConversation = conversation.copy(
                    lastStatus = conversation.lastStatus.copy(showingHiddenContent = showing)
            )
            saveConversationToDb(newConversation)
        }
    }

    fun remove(position: Int) {
        conversations.value?.getOrNull(position)?.let { conversation ->
            /* this is not ideal since deleting last post from an conversation
               should not delete the conversation but show another post of the conversation */
            timelineCases.delete(conversation.lastStatus.id)
            Single.fromCallable {
                        database.conversationDao().delete(conversation)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
    }

    private fun saveConversationToDb(conversation: ConversationEntity) {
        Single.fromCallable {
                    database.conversationDao().insert(conversation)
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    override fun onCleared() {
        disposables.dispose()
    }

}