package io.getstream.chat.android.client.query.logic

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.Result

public interface QueryChannelRequestHandler {

    public fun setCurrentRequest(request: QueryChannelsRequest)

    public suspend fun queryOffline(pagination: AnyChannelPaginationRequest)

    public suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest)
}
