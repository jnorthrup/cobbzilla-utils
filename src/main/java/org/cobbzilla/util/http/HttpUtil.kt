package org.cobbzilla.util.http

import lombok.Cleanup
import org.apache.commons.exec.CommandLine
import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.*
import org.apache.http.entity.InputStreamEntity
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.cobbzilla.util.collection.NameAndValue
import org.cobbzilla.util.string.StringUtil
import org.cobbzilla.util.system.CommandResult
import org.cobbzilla.util.system.CommandShell
import org.cobbzilla.util.system.Sleep
import org.slf4j.Logger

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.LinkedHashMap

import com.google.common.net.HttpHeaders.CONTENT_DISPOSITION
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.hexnow
import org.cobbzilla.util.http.HttpContentTypes.contentType
import org.cobbzilla.util.http.HttpStatusCodes.NO_CONTENT
import org.cobbzilla.util.http.URIUtil.getFileExt
import org.cobbzilla.util.io.FileUtil.getDefaultTempDir
import org.cobbzilla.util.string.StringUtil.CRLF
import org.cobbzilla.util.system.Sleep.sleep

object HttpUtil {

    val DEFAULT_CERT_NAME = "ssl-https"
    private val log = org.slf4j.LoggerFactory.getLogger(HttpUtil::class.java)

    val DEFAULT_RETRIES = 3

    // from: http://stackoverflow.com/a/13592567
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun queryParams(url: URL, encoding: String = StringUtil.UTF8): Map<String, String> {
        val query_pairs = LinkedHashMap<String, String>()
        val query = url.query
        val pairs = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            query_pairs[URLDecoder.decode(pair.substring(0, idx), encoding)] = URLDecoder.decode(pair.substring(idx + 1), encoding)
        }
        return query_pairs
    }

    @Throws(IOException::class)
    operator fun get(urlString: String): InputStream {
        val url = URL(urlString)
        val urlConnection = url.openConnection()
        return urlConnection.getInputStream()
    }

    @Throws(IOException::class)
    fun upload(url: String,
               file: File,
               headers: Map<String, String>?): HttpResponseBean {
        @Cleanup val client = HttpClients.createDefault()
        val method = HttpPost(url)
        val fileBody = FileBody(file)
        val builder = MultipartEntityBuilder.create().addPart("file", fileBody)
        method.entity = builder.build()

        if (headers != null) {
            for ((key, value) in headers) {
                method.addHeader(BasicHeader(key, value))
            }
        }

        @Cleanup val response = client.execute(method)

        return HttpResponseBean()
                .setEntityBytes(EntityUtils.toByteArray(response.entity))
                .setHttpHeaders(response.allHeaders)
                .setStatus(response.statusLine.statusCode)
    }

    @Throws(IOException::class)
    fun url2file(url: String, file: String?): File {
        return url2file(url, if (file == null) null else File(file), DEFAULT_RETRIES)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun url2file(url: String, file: File? = null, retries: Int = DEFAULT_RETRIES): File {
        var file = file
        if (file == null) file = File.createTempFile("url2file-", getFileExt(url), getDefaultTempDir())
        var lastException: IOException? = null
        var sleep: Long = 100
        for (i in 0 until retries) {
            try {
                @Cleanup val `in` = get(url)
                @Cleanup val out = FileOutputStream(file!!)
                IOUtils.copy(`in`, out)
                lastException = null
                break
            } catch (e: IOException) {
                lastException = e
                sleep(sleep, "waiting to possibly retry after IOException: $lastException")
                sleep *= 5
            }

        }
        if (lastException != null) throw lastException
        return file
    }

    @Throws(IOException::class)
    fun url2string(url: String): String {
        @Cleanup val `in` = get(url)
        val out = ByteArrayOutputStream()
        IOUtils.copy(`in`, out)
        return out.toString()
    }

    @Throws(IOException::class)
    fun getResponse(requestBean: HttpRequestBean): HttpResponseBean {
        val clientBuilder = requestBean.initClientBuilder(HttpClients.custom())
        @Cleanup val client = clientBuilder.build()
        return getResponse(requestBean, client)
    }

    @Throws(IOException::class)
    fun getResponse(requestBean: HttpRequestBean, client: HttpClient): HttpResponseBean {

        if (requestBean.hasStream()) return getStreamResponse(requestBean)

        val bean = HttpResponseBean()

        val request = initHttpRequest(requestBean)

        for (header in requestBean.headers) {
            request.setHeader(header.name, header.value)
        }

        val response = client.execute(request)

        for (header in response.allHeaders) {
            bean.addHeader(header.name, header.value)
        }

        bean.status = response.statusLine.statusCode
        if (response.statusLine.statusCode != NO_CONTENT) {
            bean.contentLength = response.entity.contentLength
            val contentType = response.entity.contentType
            if (contentType != null) {
                bean.contentType = contentType.value
            }
            @Cleanup val content = response.entity.content
            bean.setEntity(content)
        }

        return bean
    }

    fun getStreamResponse(request: HttpRequestBean): HttpResponseBean {
        if (!request.hasStream()) return die("getStreamResponse: request stream was not set")
        try {
            val boundary = hexnow()
            request.withHeader(CONTENT_TYPE, "multipart/form-data; boundary=$boundary")

            @Cleanup("disconnect") val connection = URL(request.uri!!).openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = request.method
            for (header in request.headers) {
                connection.setRequestProperty(header.name!!, header.value)
            }

            @Cleanup val output = connection.outputStream
            val writer = PrintWriter(OutputStreamWriter(output, Charset.defaultCharset()), true)
            writer.append("--$boundary").append(CRLF)
            val filename = request.entity
            addStreamHeader(writer, CONTENT_DISPOSITION, "form-data; name=\"file\"; filename=\"$filename\"")
            addStreamHeader(writer, CONTENT_TYPE, contentType(filename!!))
            writer.append(CRLF).flush()
            IOUtils.copy(request.entityInputStream!!, output)
            output.flush()
            writer.append(CRLF)
            writer.append("--").append(boundary).append("--").append(CRLF).flush()

            val response = HttpResponseBean()
                    .setStatus(connection.responseCode)
                    .setHttpHeaders(connection.headerFields)
            try {
                response.setEntity(connection.inputStream)
            } catch (ioe: IOException) {
                response.setEntity(connection.errorStream)
            }

            return response
        } catch (e: Exception) {
            return die("getStreamResponse: $e", e)
        }

    }

    private fun addStreamHeader(writer: PrintWriter, name: String, value: String): PrintWriter {
        writer.append(name).append(": ").append(value).append(CRLF)
        return writer
    }

    @Throws(IOException::class)
    fun getResponse(urlString: String): HttpResponseBean {

        val bean = HttpResponseBean()
        @Cleanup val client = HttpClients.createDefault()
        val response = client.execute(HttpGet(urlString))

        for (header in response.allHeaders) {
            bean.addHeader(header.name, header.value)
        }

        bean.status = response.statusLine.statusCode
        if (response.entity != null) {
            val contentType = response.entity.contentType
            if (contentType != null) bean.contentType = contentType.value

            bean.contentLength = response.entity.contentLength
            bean.setEntity(response.entity.content)
        }

        return bean
    }

    fun initHttpRequest(requestBean: HttpRequestBean): HttpUriRequest {
        try {
            val request: HttpUriRequest
            when (requestBean.method) {
                HttpMethods.GET -> request = HttpGet(requestBean.uri)

                HttpMethods.POST -> request = HttpPost(requestBean.uri)

                HttpMethods.PUT -> request = HttpPut(requestBean.uri)

                HttpMethods.DELETE -> request = HttpDelete(requestBean.uri)

                else -> return die("Invalid request method: " + requestBean.method)
            }

            if (requestBean.hasData() && request is HttpEntityEnclosingRequestBase) {
                setData(requestBean.entity, request)
            }

            return request

        } catch (e: UnsupportedEncodingException) {
            return die("initHttpRequest: $e", e)
        }

    }

    @Throws(UnsupportedEncodingException::class)
    private fun setData(data: Any?, request: HttpEntityEnclosingRequestBase) {
        if (data == null) return
        if (data is String) {
            request.entity = StringEntity((data as String?)!!)
        } else if (data is InputStream) {
            request.entity = InputStreamEntity((data as InputStream?)!!)
        } else {
            throw IllegalArgumentException("Unsupported request entity type: " + data.javaClass.name)
        }
    }

    fun getContentType(response: HttpResponse): String? {
        val contentTypeHeader = response.getFirstHeader(CONTENT_TYPE)
        return contentTypeHeader?.value
    }

    @JvmOverloads
    fun isOk(url: String, host: String = URIUtil.getHost(url)): Boolean {
        val command = CommandLine("curl")
                .addArgument("--insecure") // since we are requested via the IP address, the cert will not match
                .addArgument("--header").addArgument("Host: $host") // pass FQDN via Host header
                .addArgument("--silent")
                .addArgument("--location")                              // follow redirects
                .addArgument("--write-out").addArgument("%{http_code}") // just print status code
                .addArgument("--output").addArgument("/dev/null")       // and ignore data
                .addArgument(url)
        try {
            val result = CommandShell.exec(command)
            val statusCode = result.stdout
            return result.isZeroExitStatus && statusCode != null && statusCode.trim { it <= ' ' }.startsWith("2")

        } catch (e: IOException) {
            log.warn("isOk: Error fetching $url with Host header=$host: $e")
            return false
        }

    }

    fun isOk(url: String, host: String, maxTries: Int, sleepUnit: Long): Boolean {
        var sleep = sleepUnit
        for (i in 0 until maxTries) {
            if (i > 0) {
                Sleep.sleep(sleep)
                sleep *= 2
            }
            if (isOk(url, host)) return true
        }
        return false
    }
}
