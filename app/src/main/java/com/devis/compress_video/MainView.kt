package com.devis.compress_video

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Created by devis on 22/06/20
 */

interface MainView : CoroutineScope {

    val job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun showLoading()
    fun stopLoading()
    fun onSuccessPostVideo(result: ResultMdl)
    fun onErrorPostVideo(message: String)

}