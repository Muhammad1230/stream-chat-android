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

package io.getstream.chat.android.client.parser2.adapters

import android.os.Build
import androidx.annotation.RequiresApi
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.utils.threadLocal
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.logging.StreamLog
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

private const val TAG = "DateAdapter"

@RequiresApi(Build.VERSION_CODES.O)
@InternalStreamChatApi
public class DateAdapter : JsonAdapter<Date>() {

    private companion object {
        const val DATE_FORMAT_SECONDS = "yyyy-MM-dd'T'HH:mm:ss"
        const val DATE_FORMAT_MICRO = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        const val UTC_ID = "UTC"
        // const val DATE_FORMAT_WITHOUT_NANOSECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }

    private val dateFormatPrecisionFromString: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .appendPattern(DATE_FORMAT_SECONDS)
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 9, true)
            .appendPattern("'Z'")
            .toFormatter(Locale.US)

    private val dateFormatPrecisionToString: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .appendPattern(DATE_FORMAT_SECONDS)
            .appendFraction(ChronoField.MICRO_OF_SECOND, 6, 9, true)
            .appendPattern("'Z'")
            .toFormatter(Locale.US)

    private val dataFormatPattern: DateTimeFormatter =
        DateTimeFormatterBuilder().appendPattern(DATE_FORMAT_MICRO).toFormatter(Locale.US)

    private val dateFormatWithoutNanoseconds: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .appendPattern(DATE_FORMAT_SECONDS)
            .appendPattern("'Z'")
            .toFormatter(Locale.US)

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val rawValue = Instant.ofEpochMilli(value.time)
                .atZone(ZoneId.of(UTC_ID))
                .format(dateFormatPrecisionToString)

            val testValue = Date().toInstant()
                .atZone(ZoneId.of(UTC_ID))
                .format(dateFormatPrecisionToString)

            val testValue2 = LocalDateTime.now().format(dateFormatPrecisionToString)
            val testValue22 = LocalDateTime.now().format(dataFormatPattern)

            val testValue3 = LocalDateTime
                .parse("2022-08-24T21:37:48.657036635Z", dateFormatPrecisionFromString)
                .format(dateFormatPrecisionToString)

            StreamLog.d("DateAdapter") { "parsed date: $rawValue" }
            StreamLog.d("DateAdapter") { "testValue: $testValue" }
            StreamLog.d("DateAdapter") { "testValue2: $testValue2" }
            StreamLog.d("DateAdapter") { "testValue22: $testValue22" }
            StreamLog.d("DateAdapter") { "testValue3: $testValue3" }

            writer.value(rawValue)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @FromJson
    override fun fromJson(reader: JsonReader): Date? {
        val nextValue = reader.peek()
        if (nextValue == JsonReader.Token.NULL) {
            reader.skipValue()
            return null
        }

        val rawValue = reader.nextString()
        return if (rawValue.isEmpty()) {
            null
        } else {
            try {
                rawValue.parseDate(dateFormatPrecisionFromString)
            } catch (_: Throwable) {
                try {
                    rawValue.parseDate(dateFormatWithoutNanoseconds)
                } catch (e: Throwable) {
                    StreamLog.d(TAG) { "It was not possible to parse the date: $rawValue. Error message: ${e.message}" }
                    null
                }
            }
        }
    }

    private fun String.parseDate(dateTimeFormatter: DateTimeFormatter): Date {
        return LocalDateTime.parse(this, dateTimeFormatter)
            .atZone(ZoneId.of(UTC_ID))
            .toInstant()
            .toEpochMilli()
            .let(::Date)
    }
}
