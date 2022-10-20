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

package io.getstream.realm.repository.factory

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.realm.repository.RealmAttachmentRepository
import io.getstream.realm.repository.RealmChannelConfigRepository
import io.getstream.realm.repository.RealmChannelRepository
import io.getstream.realm.repository.RealmMessageRepository
import io.getstream.realm.repository.RealmQueryChannelsRepository
import io.getstream.realm.repository.RealmReactionRepository
import io.getstream.realm.repository.RealmSyncStateRepository
import io.getstream.realm.repository.RealmUserRepository
import io.realm.kotlin.Realm

public class RealmRepositoryFactory(private val realm: Realm) : RepositoryFactory {
    override fun createUserRepository(): UserRepository = RealmUserRepository(realm)

    override fun createChannelConfigRepository(): ChannelConfigRepository = RealmChannelConfigRepository(realm)

    override fun createQueryChannelsRepository(): QueryChannelsRepository =
        RealmQueryChannelsRepository(realm)

    override fun createSyncStateRepository(): SyncStateRepository = RealmSyncStateRepository(realm)

    override fun createAttachmentRepository(): AttachmentRepository = RealmAttachmentRepository(realm)

    override fun createReactionRepository(
        getUser: suspend (userId: String) -> User,
    ): ReactionRepository = RealmReactionRepository(realm)

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = RealmMessageRepository(realm)

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = RealmChannelRepository(realm)
}
