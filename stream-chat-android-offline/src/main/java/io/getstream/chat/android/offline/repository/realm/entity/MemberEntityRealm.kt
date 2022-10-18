package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.realm.utils.toDate
import io.getstream.chat.android.offline.repository.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.Date

internal class MemberEntityRealm : RealmObject {
    @PrimaryKey
    var user_id: String = ""

    var user: UserEntityRealm? = null

    /** the user's role, user, moderator or admin */
    var role: String = ""

    /** when the user became a member */
    var created_at: RealmInstant? = null

    /** when the membership data was last updated */
    var updated_at: RealmInstant? = null

    /** if this is an invite */
    var is_invited: Boolean = false

    /** the date the invite was accepted */
    var invite_accepted_at: RealmInstant? = null

    /** the date the invite was rejected */
    var invite_rejected_at: RealmInstant? = null

    /** if channel member is shadow banned */
    var shadow_banned: Boolean = false

    /** If channel member is banned. */
    var banned: Boolean = false

    /** The user's channel-level role. */
    var channel_role: String? = null
}

internal fun Member.toRealm(): MemberEntityRealm {
    val thisMember = this

    return MemberEntityRealm().apply {
        user_id = thisMember.user.id
        user = thisMember.user.toRealm()
        created_at = thisMember.createdAt?.toRealmInstant()
        updated_at = thisMember.updatedAt?.toRealmInstant()
        is_invited = thisMember.isInvited ?: false
        invite_accepted_at = thisMember.inviteAcceptedAt?.toRealmInstant()
        invite_rejected_at = thisMember.inviteRejectedAt?.toRealmInstant()
        shadow_banned = thisMember.shadowBanned
        banned = thisMember.banned
        channel_role = thisMember.channelRole
    }
}

internal fun MemberEntityRealm.toDomain(): Member =
    Member(
        user = user?.toDomain() ?: User(),
        createdAt = created_at?.toDate(),
        updatedAt = updated_at?.toDate(),
        isInvited = is_invited,
        inviteAcceptedAt = invite_accepted_at?.toDate(),
        inviteRejectedAt = invite_rejected_at?.toDate(),
        shadowBanned = shadow_banned,
        banned = banned,
        channelRole = channel_role,
    )
