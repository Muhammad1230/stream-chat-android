/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelData
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.client.utils.buffer.StartStopBuffer
import io.getstream.chat.android.state.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomDateAfter
import io.getstream.chat.android.test.randomDateBefore
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChannelStateLogicTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @BeforeEach
    fun setup() {
        channelStateLogic = ChannelStateLogic(
            mutableState,
            globalMutableState = globalMutableState,
            searchLogic = SearchLogic(mutableState),
            attachmentUrlValidator = attachmentUrlValidator,
            coroutineScope = testCoroutines.scope,
            countBuffer = StartStopBuffer()
        )
    }

    private val user = randomUser()
    private var _messages: Map<String, Message> = emptyMap()
    private val _unreadCount: MutableStateFlow<Int> = MutableStateFlow(0)
    private val unreadCount = randomInt()
    private val _read: MutableStateFlow<ChannelUserRead> = MutableStateFlow(
        ChannelUserRead(user, lastMessageSeenDate = Date(Long.MIN_VALUE), unreadMessages = unreadCount)
    )
    private val _channelData: MutableStateFlow<ChannelData> =
        MutableStateFlow(ChannelData(randomChannel(), emptySet()))
    private var _reads: Map<String, ChannelUserRead> = emptyMap()
    private val _insideSearch = MutableStateFlow(false)
    private val _watcherCount = MutableStateFlow(0)
    private val _membersCount = MutableStateFlow(0)
    private val _channelConfig = MutableStateFlow(Config())
    private val channelId = "channelId"
    private val _endOfNewerMessages = MutableStateFlow(true)
    private val _cachedMessages = MutableStateFlow<Map<String, Message>>(emptyMap())

    @Suppress("UNCHECKED_CAST")
    private val mutableState: ChannelMutableState = mock { mock ->
        on(mock.unreadCount) doReturn _unreadCount
        on(mock.read) doReturn _read
        on(mock.cid) doReturn randomCID()
        on(mock.channelId) doReturn channelId
        on(mock.channelData) doReturn _channelData
        on(mock.insideSearch) doReturn _insideSearch
        on(mock.watcherCount) doReturn _watcherCount
        on(mock.membersCount) doReturn _membersCount
        on(mock.channelConfig) doReturn _channelConfig
        on(mock.messageList) doReturn MutableStateFlow(emptyList())
        on(mock.endOfNewerMessages) doReturn _endOfNewerMessages
        on(mock.cachedLatestMessages) doReturn _cachedMessages
    }
    private val globalMutableState: GlobalMutableState = mock {
        on(it.channelMutes) doReturn MutableStateFlow(emptyList())
        on(it.user) doReturn MutableStateFlow(user)
    }

    private val attachmentUrlValidator: AttachmentUrlValidator = mock {
        on(it.updateValidAttachmentsUrl(any(), any())) doAnswer { invocationOnMock ->
            invocationOnMock.arguments[0] as List<Message>
        }
    }

    @BeforeEach
    fun setUp() {
        GlobalMutableState.instance = globalMutableState

        _messages = emptyMap()
        _unreadCount.value = 0
        _read.value = ChannelUserRead(user, lastMessageSeenDate = Date(Long.MIN_VALUE), unreadMessages = unreadCount)
        _channelData.value = ChannelData(randomChannel(), emptySet())
        _reads = emptyMap()
        _insideSearch.value = false
        _watcherCount.value = 0
        _watcherCount.value = 0
    }

    private lateinit var channelStateLogic: ChannelStateLogic

    @Test
    fun `given a message is outdated it should not be upserted`() {
        val createdAt = randomDate()
        val createdLocallyAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)
        val oldUpdatedAt = randomDateBefore(updatedAt.time)
        whenever(mutableState.visibleMessages) doReturn MutableStateFlow(_messages)

        val recentMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = oldUpdatedAt,
            updatedLocallyAt = oldUpdatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogic.upsertMessage(recentMessage)
        channelStateLogic.upsertMessage(oldMessage)

        // Only the new message is available
        _messages `should not be equal to` mapOf(recentMessage.id to recentMessage)
    }

    @Test
    fun `new messages should increment the unread count`() {
        val createdAt = randomDate()
        val oldCreatedAt = randomDateBefore(createdAt.time)
        val oldMessages = List(positiveRandomInt(20)) { randomMessage() }
        whenever(mutableState.visibleMessages) doReturn MutableStateFlow(oldMessages.associateBy(Message::id))
        val newUnreadCount = randomInt()
        whenever(mutableState.read) doReturn MutableStateFlow(
            ChannelUserRead(
                user = user,
                lastMessageSeenDate = Date(Long.MIN_VALUE),
                unreadMessages = newUnreadCount
            )
        )

        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = oldCreatedAt,
            createdLocallyAt = oldCreatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogic.incrementUnreadCountIfNecessary(oldMessage)
        verify(mutableState).increaseReadWith(oldMessage)
    }

    @Test
    fun `old messages should NOT increment the unread count`() {
        // The last message is really new.
        whenever(mutableState.read) doReturn MutableStateFlow(
            ChannelUserRead(user, lastMessageSeenDate = Date(Long.MAX_VALUE))
        )
        val oldMessages = List(positiveRandomInt(20)) { randomMessage() }
        whenever(mutableState.visibleMessages) doReturn MutableStateFlow(oldMessages.associateBy(Message::id))

        repeat(3) {
            channelStateLogic.incrementUnreadCountIfNecessary(oldMessages.random())
        }

        _unreadCount.value `should be equal to` 0
    }

    @Test
    fun `Given TypingStartEvent contains the currently logged in userId Should not update typing events`() {
        val typingStartEvent = randomTypingStartEvent(user = user)

        channelStateLogic.setTyping(typingStartEvent.user.id, typingStartEvent)

        verify(mutableState, times(0)).updateTypingEvents(any(), any())
        verify(globalMutableState, times(0)).tryEmitTypingEvent(any(), any())
    }

    @Test
    fun `given loading message by id, message list should enter search state`() {
        val message = randomMessage()
        val channel = randomChannel(messages = listOf(message))
        val request = QueryChannelRequest().withMessages(direction = Pagination.AROUND_ID, messageId = message.id, 30)

        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).setInsideSearch(true)
    }

    @Test
    fun `given message list refreshed, message list should exit search state`() {
        val message = randomMessage()
        val channel = randomChannel(messages = listOf(message))
        val request = QueryChannelRequest().apply {
            shouldRefresh = true
        }.withMessages(30)

        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).setInsideSearch(false)
    }

    @Test
    fun `given messageLimit is 0, messages should not be upserted`() {
        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val request = QueryChannelRequest().withMessages(0)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState, times(0)).updateCachedLatestMessages(any())
        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `given inside search should not upsert messages when messages are not coming from scroll update`() {
        _insideSearch.value = true

        val message = randomMessage()

        channelStateLogic.updateDataFromChannel(
            randomChannel(messages = listOf(message)),
            shouldRefreshMessages = false,
            scrollUpdate = false,
            isNotificationUpdate = false,
            messageLimit = 30
        )

        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `given inside search, when new page arrives, should be upserted`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val filteringRequest = QueryChannelPaginationRequest(1)
            .withMessages(Pagination.GREATER_THAN, randomString(), 1)

        channelStateLogic.propagateChannelQuery(channel, filteringRequest)
        verify(mutableState).upsertMessages(any())
    }

    @Test
    fun `given inside search, when notification message arrives, should not be upserted`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val request = QueryChannelRequest().apply { isNotificationUpdate = true }.withMessages(1)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `given inside search, new message should be added to cached messages`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val request = QueryChannelRequest().withMessages(1)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).updateCachedLatestMessages(any())
    }

    @Test
    fun `given refresh messages is true, new messages should replace the old ones`() {
        val messages = listOf(randomMessage(), randomMessage())
        val channel: Channel = randomChannel(messages = messages)
        val request = QueryChannelRequest().apply { shouldRefresh = true }.withMessages(1)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).setMessages(any())
    }

    @Test
    fun `given inside search, if message update comes, should update the message`() {
        _insideSearch.value = true
        val message = randomMessage()
        whenever(mutableState.visibleMessages) doReturn MutableStateFlow(mapOf(message.id to message))
        val updatedMessage = message.copy(
            text = "new text",
            updatedAt = randomDateAfter((message.updatedAt ?: message.updatedLocallyAt ?: NEVER).time),
            updatedLocallyAt = randomDateAfter((message.updatedLocallyAt ?: message.updatedAt ?: NEVER).time)
        )

        channelStateLogic.upsertMessage(updatedMessage)

        verify(mutableState).upsertMessages(listOf(updatedMessage))
    }
}
