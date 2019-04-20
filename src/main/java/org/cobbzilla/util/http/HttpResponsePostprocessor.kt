package org.cobbzilla.util.http

interface HttpResponsePostprocessor {

    fun postProcess(response: HttpResponseBean): HttpResponseBean

}
