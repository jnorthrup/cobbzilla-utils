package org.cobbzilla.util.http;

import org.cobbzilla.util.collection.MapBuilder;
import org.cobbzilla.util.collection.NameAndValue;

import java.util.Map;

import static org.apache.commons.lang3.StringEscapeUtils.*;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class HttpContentTypes {

    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_CSV = "text/csv";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpg";
    public static final String IMAGE_GIF = "image/gif";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String UNKNOWN = APPLICATION_OCTET_STREAM;
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_JAR = "application/java-archive";
    public static final String APPLICATION_GZIP = "application/gzip";
    // useful when constructing HttpRequestBeans that will be used against a JSON API

    private static NameAndValue[] nvHttp(String type) { return new NameAndValue[]{new NameAndValue(CONTENT_TYPE, type)}; }

    public static final NameAndValue[] NV_HTTP_JSON = nvHttp(APPLICATION_JSON);
    public static final NameAndValue[] NV_HTTP_XML = nvHttp(APPLICATION_XML);

    public static final Map<String, NameAndValue[]> HTTP_CONTENT_TYPES = MapBuilder.build(new Object[][] {
            { APPLICATION_JSON, NV_HTTP_JSON },
            { APPLICATION_XML, NV_HTTP_XML },
    });

    public static String contentType(String name) {
        final int dot = name.lastIndexOf('.');
        final String ext = (dot != -1 && dot != name.length()-1) ? name.substring(dot+1) : name;
        switch (ext) {
            case "htm": case "html": return TEXT_HTML;
            case "png":              return IMAGE_PNG;
            case "jpg": case "jpeg": return IMAGE_JPEG;
            case "gif":              return IMAGE_GIF;
            case "xml":              return APPLICATION_XML;
            case "pdf":              return APPLICATION_PDF;
            case "json":             return APPLICATION_JSON;
            case "gz": case "tgz":   return APPLICATION_GZIP;
            case "zip":              return APPLICATION_ZIP;
            case "jar":              return APPLICATION_JAR;
            case "txt":              return TEXT_PLAIN;
            case "csv":              return TEXT_CSV;
            default: return die("contentType: no content-type could be determined for name: "+name);
        }
    }

    public static String fileExt (String contentType) {
        switch (contentType) {
            case TEXT_HTML:        return ".html";
            case TEXT_PLAIN:       return ".txt";
            case TEXT_CSV:         return ".csv";
            case IMAGE_PNG:        return ".png";
            case IMAGE_JPEG:       return ".jpeg";
            case IMAGE_GIF:        return ".gif";
            case APPLICATION_XML:  return ".xml";
            case APPLICATION_PDF:  return ".pdf";
            case APPLICATION_JSON: return ".json";
            case APPLICATION_ZIP: return ".zip";
            case APPLICATION_GZIP: return ".tar.gz";
            default: return die("fileExt: no file extension could be determined for content-type: "+contentType);
        }
    }

    public static String fileExtNoDot (String contentType) {
        return fileExt(contentType).substring(1);
    }

    public static String escape(String mime, String data) {
        switch (mime) {
            case APPLICATION_XML: return escapeXml10(data);
            case TEXT_HTML: return escapeHtml4(data);
        }
        return data;
    }

    public static String unescape(String mime, String data) {
        if (empty(data)) return data;
        switch (mime) {
            case APPLICATION_XML: return unescapeXml(data);
            case TEXT_HTML: return unescapeHtml4(data);
        }
        return data;
    }

    public static String multipartWithBoundary(String boundary) { return "multipart/form-data; boundary=" + boundary; }

}
