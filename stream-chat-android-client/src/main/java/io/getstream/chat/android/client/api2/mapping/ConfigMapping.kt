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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.models.Config

internal fun ConfigDto.toDomain(): Config =
    Config(
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name ?: "",
        typingEventsEnabled = typingEvents,
        readEventsEnabled = readEvents,
        connectEventsEnabled = connectEvents,
        searchEnabled = search,
        isReactionsEnabled = reactions,
        isThreadEnabled = replies,
        muteEnabled = mutes,
        uploadsEnabled = uploads,
        urlEnrichmentEnabled = urlEnrichment,
        customEventsEnabled = customEvents,
        pushNotificationsEnabled = pushNotifications,
        messageRetention = messageRetention,
        maxMessageLength = maxMessageLength,
        automod = automod,
        automodBehavior = automodBehavior,
        blocklistBehavior = blocklistBehavior ?: "",
        commands = commands.map(CommandDto::toDomain),
    )
