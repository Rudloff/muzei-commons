/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pro.rudloff.muzei.commons

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal interface CommonsService {

    companion object {

        private fun createService(): CommonsService {
            val okHttpClient = OkHttpClient.Builder().build()

            val retrofit = Retrofit.Builder()
                    .baseUrl("https://commons.wikimedia.org/")
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

            return retrofit.create<CommonsService>(CommonsService::class.java)
        }

        @Throws(IOException::class)
        internal fun todayQuery(): CommonsService.Response {
            return createService().api(
                action = "query",
                prop = "images",
                titles = "Template:Potd/" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            ).execute().body()
                ?: throw IOException("Response was null")
        }

        @Throws(IOException::class)
        internal fun photoInfo(title: String): CommonsService.Response {
            return createService().api(
                action = "query",
                prop = "imageinfo",
                titles = title,
                iiprop = "url|user|canonicaltitle"
            ).execute().body()
                ?: throw IOException("Response was null")
        }
    }

    @GET("w/api.php")
    fun api(
        @Query("action") action: String,
        @Query("prop") prop: String,
        @Query("titles") titles: String,
        @Query("format") format: String = "json",
        @Query("imlimit") imlimit: Int = 1,
        @Query("formatversion") formatversion: Int = 2,
        @Query("iiprop") iiprop: String = "",
        @Query("iiurlwidth") iiurlwidth: Int = 1920
    ): Call<Response>

    data class Response(
        val query: ApiQuery
    )

    data class ApiQuery(
        val pages: List<Page>
    )

    data class Page(
        val images: List<Image>?,
        val imageinfo: List<Imageinfo>?
    )

    data class Image(
        val title: String
    )

    data class Imageinfo(
        val url: String,
        val user: String,
        val descriptionurl: String,
        val thumburl: String,
        val canonicaltitle: String
    )
}
