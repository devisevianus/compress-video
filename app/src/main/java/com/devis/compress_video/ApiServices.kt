package com.devis.compress_video

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Created by devis on 22/06/20
 */

interface ApiServices {

    @Multipart
    @POST("uploadtest.php")
    suspend fun postVideo(
        @Part("video\"; filename=\"video_test.mp4\" ") video: RequestBody
    ): Response<ResultMdl>

}