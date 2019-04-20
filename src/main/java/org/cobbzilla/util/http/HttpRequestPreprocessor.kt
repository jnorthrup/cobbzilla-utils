package org.cobbzilla.util.http

interface HttpRequestPreprocessor {

    fun preProcess(request: HttpRequestBean): HttpRequestBean

}
