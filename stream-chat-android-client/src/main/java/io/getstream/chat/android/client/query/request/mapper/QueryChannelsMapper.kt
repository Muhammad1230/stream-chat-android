package io.getstream.chat.android.client.query.request.mapper

import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.query.request.QueryChannelsPaginationRequest

internal fun QueryChannelsPaginationRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    return AnyChannelPaginationRequest().apply {
        this.channelLimit = originalRequest.channelLimit
        this.channelOffset = originalRequest.channelOffset
        this.messageLimit = originalRequest.messageLimit
        this.sort = originalRequest.sort
    }
}
