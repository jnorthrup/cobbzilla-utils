package org.cobbzilla.util.chef

class VendorDatabagSetting {

    var path: String? = null
    var shasum: String? = null
    var isBlock_ssh = false

    constructor(path: String, shasum: String) {
        path = path
        shasum = shasum
    }

    @java.beans.ConstructorProperties("path", "shasum", "block_ssh")
    constructor(path: String, shasum: String, block_ssh: Boolean) {
        this.path = path
        this.shasum = shasum
        this.isBlock_ssh = block_ssh
    }

    constructor() {}
}
