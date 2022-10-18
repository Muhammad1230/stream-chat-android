package io.getstream.chat.android.offline.repository.realm.repository

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.realm.entity.ReactionEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.toDomain
import io.getstream.chat.android.offline.repository.realm.entity.toRealm
import io.getstream.chat.android.offline.repository.realm.utils.toRealmInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import java.util.Date

internal class RealmReactionRepository(private val realm: Realm) : ReactionRepository {

    override suspend fun insertReaction(reaction: Reaction) {
        realm.writeBlocking {
            copyToRealm(reaction.toRealm())
        }
    }

    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        val query = "message_id == $messageId AND user_id == $userId AND deleted_at == ${deletedAt.toRealmInstant()}"
        val entities = realm.query<ReactionEntityRealm>(query).find()

        realm.writeBlocking {
            delete(entities)
        }
    }

    override suspend fun selectReactionById(id: Int): Reaction? =
        realm.query<ReactionEntityRealm>("id == '$id'")
            .first()
            .find()
            ?.toDomain()

    override suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction> {
        val idsList = ids.joinToString(prefix = "{ ", postfix = " }")

        return realm.query<ReactionEntityRealm>("id IN $idsList")
            .find()
            .map { reactionEntity -> reactionEntity.toDomain() }
    }

    override suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int> =
        realm.query<ReactionEntityRealm>("sync_status == ${syncStatus.status}")
            .find()
            .map { reactionEntity -> reactionEntity.id }

    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> =
        realm.query<ReactionEntityRealm>("sync_status == ${syncStatus.status}")
            .find()
            .map { reactionEntity -> reactionEntity.toDomain() }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction? {
        val query = "type == $reactionType AND message_id == $messageId AND user_id == $userId"

        return realm.query<ReactionEntityRealm>(query)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<Reaction> {
        val query = "message_id == $messageId AND user_id == $userId"

        return realm.query<ReactionEntityRealm>(query)
            .find()
            .map { reactionEntity ->
                reactionEntity.toDomain()
            }
    }

    override suspend fun clear() {
        // Implement
    }
}
