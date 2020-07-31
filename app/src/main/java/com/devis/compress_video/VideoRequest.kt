package com.devis.compress_video

import okhttp3.RequestBody

/**
 * Created by devis on 22/06/20
 */

interface VideoRequest {

    suspend fun postVideo(video: RequestBody): ResultState<ResultMdl>

}