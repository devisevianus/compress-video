package com.devis.compress_video

import okhttp3.RequestBody

/**
 * Created by devis on 22/06/20
 */

class VideoRepository : VideoRequest {

    private val mClient = ApiClient("https://freehostingtesting.000webhostapp.com/androidtest/")

    override suspend fun postVideo(video: RequestBody): ResultState<ResultMdl> {
        return fetchState {
            val response = mClient.getApiServices().postVideo(video)
            if (response.isSuccessful) {
                ResultState.Success(response.body())
            } else {
                ResultState.Error(response.body()?.result)
            }
        }
    }
}