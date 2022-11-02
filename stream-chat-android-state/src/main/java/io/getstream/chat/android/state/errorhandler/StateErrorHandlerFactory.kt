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

package io.getstream.chat.android.state.errorhandler

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.offline.errorhandler.internal.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry

public class StateErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(channelRepository: ChannelRepository): ErrorHandler {
        val scope = StateRegistry.get().scope
        val logicRegistry = LogicRegistry.get()
        val clientState = ChatClient.instance().clientState

        val deleteReactionErrorHandler = DeleteReactionErrorHandlerImpl(
            scope = scope,
            logic = logicRegistry,
            clientState = clientState,
        )

        val createChannelErrorHandler = CreateChannelErrorHandlerImpl(
            scope = scope,
            clientState = clientState,
            channelRepository = channelRepository,
        )

        val queryMembersErrorHandler = QueryMembersErrorHandlerImpl(
            scope = scope,
            clientState = clientState,
            channelRepository = channelRepository
        )

        val sendReactionErrorHandler = SendReactionErrorHandlerImpl(scope = scope, clientState = clientState)

        return StateErrorHandler(
            deleteReactionErrorHandler = deleteReactionErrorHandler,
            createChannelErrorHandler = createChannelErrorHandler,
            queryMembersErrorHandler = queryMembersErrorHandler,
            sendReactionErrorHandler = sendReactionErrorHandler,
        )
    }
}
