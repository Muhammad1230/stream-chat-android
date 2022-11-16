package io.getstream.chat.docs.kotlin.ui.guides

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * [Building A Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-message-list-screen/)
 */
class BuildingAMessageListScreen : Fragment() {

    private lateinit var messageListView: MessageListView
    private lateinit var messageListHeaderView: MessageListHeaderView
    private lateinit var messageComposerView: MessageComposerView

    fun usage() {
        // Create view models
        val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

        // Bind view models
        messageListHeaderViewModel.bindView(messageListHeaderView, viewLifecycleOwner)
        messageListViewModel.bindView(messageListView, viewLifecycleOwner)
        messageComposerViewModel.bindView(messageComposerView, viewLifecycleOwner)

        // Let both message list header and message input know when we open a thread
        messageListViewModel.mode.observe(this) { mode ->
            when (mode) {
                is MessageMode.MessageThread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                }
                MessageMode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                }
            }
        }

        // Let the message composer know when we are replying to a message
        messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }

        // Let the message composer know when we are editing a message
        messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }

        // Handle navigate up state
        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                // Handle navigate up
            }
        }

        // Handle back button behaviour correctly when you're in a thread
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        messageListHeaderView.setBackButtonClickListener(backHandler)
    }
}
