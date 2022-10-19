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

package io.getstream.chat.android.offline.repository.factory.internal

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
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.internal.DatabaseChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.DatabaseChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.DatabaseAttachmentRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.DatabaseMessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.DatabaseReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.DatabaseSyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.DatabaseUserRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmChannelRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmMessageRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmQueryChannelsRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmReactionRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmSyncStateRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmUserRepository
import io.realm.kotlin.Realm

private const val DEFAULT_CACHE_SIZE = 100

@Suppress("UnusedPrivateMember")
internal class DatabaseRepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User,
    private val realm: Realm,
) : RepositoryFactory {

    private var repositoriesCache: MutableMap<Class<out Any>, Any> = mutableMapOf()

    private fun roomUserRepository(): DatabaseUserRepository {
        val databaseUserRepository = repositoriesCache[UserRepository::class.java] as? DatabaseUserRepository?

        return databaseUserRepository ?: run {
            DatabaseUserRepository(database.userDao(), DEFAULT_CACHE_SIZE).also { repository ->
                repositoriesCache[UserRepository::class.java] = repository
            }
        }
    }

    private fun realmUserRepository(): RealmUserRepository = RealmUserRepository(realm)

    override fun createUserRepository(): UserRepository = realmUserRepository()

    override fun createChannelConfigRepository(): ChannelConfigRepository {
        val databaseChannelConfigRepository =
            repositoriesCache[ChannelConfigRepository::class.java] as? DatabaseChannelConfigRepository?

        return databaseChannelConfigRepository ?: run {
            DatabaseChannelConfigRepository(database.channelConfigDao()).also { repository ->
                repositoriesCache[ChannelConfigRepository::class.java] = repository
            }
        }
    }

    private fun realmChannelRepository(): ChannelRepository = RealmChannelRepository(realm)

    private fun roomChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository {
        val databaseChannelRepository = repositoriesCache[ChannelRepository::class.java] as? DatabaseChannelRepository?

        return databaseChannelRepository ?: run {
            DatabaseChannelRepository(database.channelStateDao(), getUser, getMessage, DEFAULT_CACHE_SIZE)
                .also { repository ->
                    repositoriesCache[ChannelRepository::class.java] = repository
                }
        }
    }

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = realmChannelRepository()

    private fun roomQueryChannelsRepository(): DatabaseQueryChannelsRepository {
        val databaseQueryChannelsRepository =
            repositoriesCache[QueryChannelsRepository::class.java] as? DatabaseQueryChannelsRepository?

        return databaseQueryChannelsRepository ?: run {
            DatabaseQueryChannelsRepository(database.queryChannelsDao()).also { repository ->
                repositoriesCache[QueryChannelsRepository::class.java] = repository
            }
        }
    }

    private fun realmQueryChannelsRepository(): RealmQueryChannelsRepository = RealmQueryChannelsRepository(realm)

    override fun createQueryChannelsRepository(): QueryChannelsRepository = roomQueryChannelsRepository()

    private fun realmMessageRepository(): MessageRepository = RealmMessageRepository(realm)

    private fun roomMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository {
        val databaseMessageRepository = repositoriesCache[MessageRepository::class.java] as? DatabaseMessageRepository?

        return databaseMessageRepository ?: run {
            DatabaseMessageRepository(
                database.messageDao(),
                getUser,
                currentUser,
                DEFAULT_CACHE_SIZE
            ).also { repository ->
                repositoriesCache[MessageRepository::class.java] = repository
            }
        }
    }

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = realmMessageRepository()

    private fun roomReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository {
        val databaseReactionRepository =
            repositoriesCache[ReactionRepository::class.java] as? DatabaseReactionRepository?

        return databaseReactionRepository ?: run {
            DatabaseReactionRepository(database.reactionDao(), getUser).also { repository ->
                repositoriesCache[ReactionRepository::class.java] = repository
            }
        }
    }

    private fun realmReactionRepository(): RealmReactionRepository = RealmReactionRepository(realm)

    override fun createReactionRepository(
        getUser: suspend (userId: String) -> User
    ): ReactionRepository = realmReactionRepository()

    private fun roomSyncStateRepository(): DatabaseSyncStateRepository {
        val databaseSyncStateRepository =
            repositoriesCache[SyncStateRepository::class.java] as? DatabaseSyncStateRepository?

        return databaseSyncStateRepository ?: run {
            DatabaseSyncStateRepository(database.syncStateDao()).also { repository ->
                repositoriesCache[SyncStateRepository::class.java] = repository
            }
        }
    }

    private fun realmSyncStateRepository(): RealmSyncStateRepository = RealmSyncStateRepository(realm)

    override fun createSyncStateRepository(): SyncStateRepository = realmSyncStateRepository()

    override fun createAttachmentRepository(): AttachmentRepository {
        val databaseAttachmentRepository =
            repositoriesCache[AttachmentRepository::class.java] as? DatabaseAttachmentRepository?

        return databaseAttachmentRepository ?: run {
            DatabaseAttachmentRepository(database.attachmentDao()).also { repository ->
                repositoriesCache[AttachmentRepository::class.java] = repository
            }
        }
    }
}
