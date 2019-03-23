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
import java.io.IOException

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
            return createService().todayQuery.execute().body()
                    ?: throw IOException("Response was null")
        }

        @Throws(IOException::class)
        internal fun photoInfo(): CommonsService.Response {
            return createService().photoInfo.execute().body()
                    ?: throw IOException("Response was null")
        }
    }

    @get:GET("w/api.php?action=query&prop=images&titles=Template:Potd/2019-03-23&format=json&imlimit=1&formatversion=2")
    val todayQuery: Call<Response>

    @get:GET("w/api.php?action=query&prop=imageinfo&titles=File:Vents%20du%20Sud%20-%20Le%20Grau-du-Roi%2004.jpg&format=json&iiprop=url|user|canonicaltitle&formatversion=2")
    val photoInfo: Call<Response>

    data class Response(
        val query: Query
    )

    data class Query(
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
        val canonicaltitle: String
    )
}
