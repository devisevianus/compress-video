package com.devis.compress_video

import java.net.ConnectException
import javax.net.ssl.SSLException

/**
 * Created by devis on 22/06/20
 */

suspend fun <T : Any> fetchState(call: suspend () -> ResultState<T>): ResultState<T> {
    return try {
        call.invoke()
    } catch (e: ConnectException) {
        ResultState.Error(e.message)
    } catch (e: Throwable) {
        ResultState.Error(e.message)
    } catch (e: SSLException){
        ResultState.Error(e.message)
    } catch (e: Exception) {
        ResultState.Error(e.message)
    }
}