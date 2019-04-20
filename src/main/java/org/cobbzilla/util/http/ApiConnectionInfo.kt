package org.cobbzilla.util.http

import org.cobbzilla.util.reflect.ReflectionUtil.copy

class ApiConnectionInfo {

    var baseUri: String? = null
    var user: String? = null
    var password: String? = null

    // alias for when this is used in json with snake_case naming conventions
    var base_uri: String?
        get() = baseUri
        set(uri) {
            baseUri = uri
        }

    constructor(baseUri: String) {
        this.baseUri = baseUri
    }

    constructor(other: ApiConnectionInfo) {
        copy(this, other)
    }

    @java.beans.ConstructorProperties("baseUri", "user", "password")
    constructor(baseUri: String, user: String, password: String) {
        this.baseUri = baseUri
        this.user = user
        this.password = password
    }

    constructor() {}

    override fun toString(): java.lang.String {
        return "ApiConnectionInfo(baseUri=" + this.baseUri + ", user=" + this.user + ")"
    }
}
