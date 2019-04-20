package org.cobbzilla.util.http

interface HttpResponseHandler {

    fun isSuccess(request: HttpRequestBean, response: HttpResponseBean): Boolean
    fun success(request: HttpRequestBean, response: HttpResponseBean)
    fun failure(request: HttpRequestBean, response: HttpResponseBean)

}
