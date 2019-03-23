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

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import java.io.IOException

class CommonsWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "MuzeiCommons"

        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance()
            workManager.enqueue(OneTimeWorkRequestBuilder<CommonsWorker>()
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build())
        }
    }

    override fun doWork(): Result {
        val images = try {
            CommonsService.todayQuery().query.pages[0].images
        } catch (e: IOException) {
            Log.w(TAG, "Error reading Mediawiki API response", e)
            return Result.retry()
        }

        if (images!!.isEmpty()) {
            Log.w(TAG, "No photos returned from API.")
            return Result.failure()
        }

        val photo = try {
            CommonsService.photoInfo(images[0].title).query.pages[0].imageinfo?.get(0)
        } catch (e: IOException) {
            Log.w(TAG, "Error reading Mediawiki API response", e)
            return Result.retry()
        }

        val providerClient = ProviderContract.getProviderClient(
                applicationContext, "pro.rudloff.muzei.commons")
        val attributionString = applicationContext.getString(R.string.attribution)
        providerClient.addArtwork(
            Artwork().apply {
                token = photo?.url
                title = photo?.canonicaltitle?.replace("File:", "")
                byline = photo?.user
                attribution = attributionString
                persistentUri = photo?.url?.toUri()
                webUri = photo?.descriptionurl?.toUri()
            }
        )
        return Result.success()
    }
}
