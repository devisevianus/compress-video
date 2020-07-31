package com.devis.compress_video

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody

/**
 * Created by devis on 22/06/20
 */

class MainPresenter(private val mView: MainView) {

    private val mVideoRepository = VideoRepository()

    fun postVideo(video: RequestBody) {
        mView.showLoading()
        mView.launch {
            withContext(Dispatchers.Main) {
                when (val response = mVideoRepository.postVideo(video)) {
                    is ResultState.Success -> {
                        mView.stopLoading()
                        mView.onSuccessPostVideo(response.data!!)
                    }
                    is ResultState.Error -> {
                        mView.stopLoading()
                        mView.onErrorPostVideo(response.errorMessage.toString())
                    }
                }
            }
        }
    }

}