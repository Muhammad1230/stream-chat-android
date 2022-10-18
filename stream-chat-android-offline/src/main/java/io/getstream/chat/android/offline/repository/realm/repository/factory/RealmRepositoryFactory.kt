package io.getstream.chat.android.offline.repository.realm.repository.factory

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
import io.getstream.chat.android.client.persistance.repository.noop.NoOpAttachmentRepository
import io.getstream.chat.android.client.persistance.repository.noop.NoOpChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.noop.NoOpReactionRepository
import io.getstream.chat.android.client.persistance.repository.noop.NoOpSyncStateRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmChannelRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmMessageRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmQueryChannelsRepository
import io.getstream.chat.android.offline.repository.realm.repository.RealmUserRepository
import io.realm.kotlin.Realm

public class RealmRepositoryFactory(private val realm: Realm) : RepositoryFactory {
    override fun createUserRepository(): UserRepository = RealmUserRepository(realm)

    override fun createChannelConfigRepository(): ChannelConfigRepository = NoOpChannelConfigRepository

    override fun createQueryChannelsRepository(): QueryChannelsRepository =
        RealmQueryChannelsRepository(realm)

    override fun createSyncStateRepository(): SyncStateRepository = NoOpSyncStateRepository

    override fun createAttachmentRepository(): AttachmentRepository = NoOpAttachmentRepository

    override fun createReactionRepository(
        getUser: suspend (userId: String) -> User,
    ): ReactionRepository = NoOpReactionRepository

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = RealmMessageRepository(realm, getUser)

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = RealmChannelRepository(realm, getUser, getMessage)
}
