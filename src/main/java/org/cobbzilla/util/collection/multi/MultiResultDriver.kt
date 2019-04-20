package org.cobbzilla.util.collection.multi

interface MultiResultDriver {

    val result: MultiResult

    // allows the caller/user to stash things for use during execution
    var context: Map<String, Any>

    val maxConcurrent: Int
    val timeout: Long

    // called before trying calculate result
    fun before()

    fun exec(task: Any)

    // called if calculation was a success
    fun success(message: String)

    // called if calculation failed
    fun failure(message: String, e: Exception)

    // called at the end (should via finally block)
    fun after()

}
