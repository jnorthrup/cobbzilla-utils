package org.cobbzilla.util.system

class ConnectionInfo {

    var host: String? = null
    var port: Int? = null

    var username: String? = null
    var password: String? = null

    // convenience methods
    var user: String?
        get() = username
        set(user) {
            username = user
        }

    @java.beans.ConstructorProperties("host", "port", "username", "password")
    constructor(host: String, port: Int?, username: String?, password: String?) {
        this.host = host
        this.port = port
        this.username = username
        this.password = password
    }

    constructor() {}

    fun hasPort(): Boolean {
        return port != null
    }

    constructor(host: String, port: Int?) : this(host, port, null, null) {}
}
