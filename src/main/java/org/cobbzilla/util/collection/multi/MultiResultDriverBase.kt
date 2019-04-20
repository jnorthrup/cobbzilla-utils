package org.cobbzilla.util.collection.multi

import lombok.Getter
import org.apache.commons.lang3.exception.ExceptionUtils

abstract class MultiResultDriverBase : MultiResultDriver {

    @Getter
    override var result = MultiResult()
        protected set

    protected abstract fun successMessage(task: Any): String
    protected abstract fun failureMessage(task: Any): String
    @Throws(Exception::class)
    protected abstract fun run(task: Any)

    override fun before() {}
    override fun after() {}

    override fun exec(task: Any) {
        try {
            before()
            run(task)
            success(successMessage(task))
        } catch (e: Exception) {
            failure(failureMessage(task), e)
        } finally {
            after()
        }
    }

    override fun success(message: String) {
        result.success(message)
    }

    override fun failure(message: String, e: Exception) {
        result.fail(message, e.toString() + "\n- stack -\n" + ExceptionUtils.getStackTrace(e) + "\n- end stack -\n")
    }

}
