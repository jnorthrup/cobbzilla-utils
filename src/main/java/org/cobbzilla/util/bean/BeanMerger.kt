package org.cobbzilla.util.bean

import org.apache.commons.beanutils.PropertyUtilsBean

import java.beans.PropertyDescriptor

import org.cobbzilla.util.daemon.ZillaRuntime.die

object BeanMerger {

    private val propertyUtils = PropertyUtilsBean()

    fun mergeProperties(dest: Any, orig: Any) {
        merge(dest, orig, AlwaysCopy.INSTANCE)
    }

    fun mergeNotNullProperties(dest: Any, orig: Any) {
        merge(dest, orig, NotNull.INSTANCE)
    }

    private fun merge(dest: Any?, orig: Any?, evaluator: CopyEvaluator) {

        if (dest == null) throw IllegalArgumentException("No destination bean specified")
        if (orig == null) throw IllegalArgumentException("No origin bean specified")

        val origDescriptors = propertyUtils.getPropertyDescriptors(orig)
        for (origDescriptor in origDescriptors) {
            val name = origDescriptor.name
            if ("class" == name) {
                continue // No point in trying to set an object's class
            }
            if (propertyUtils.isReadable(orig, name) && propertyUtils.isWriteable(dest, name)) {
                try {
                    val value = propertyUtils.getSimpleProperty(orig, name)
                    if (evaluator.shouldCopy(name, value)) {
                        propertyUtils.setProperty(dest, name, value)
                    }
                } catch (e: NoSuchMethodException) {
                    // Should not happen
                } catch (e: Exception) {
                    die<Any>("Error copying properties: $e", e)
                }

            }
        }
    }

    private interface CopyEvaluator {
        fun shouldCopy(name: String, value: Any): Boolean
    }

    internal class AlwaysCopy : CopyEvaluator {
        override fun shouldCopy(name: String, value: Any): Boolean {
            return true
        }

        companion object {
            val INSTANCE = AlwaysCopy()
        }
    }

    internal class NotNull : CopyEvaluator {
        override fun shouldCopy(name: String, value: Any?): Boolean {
            return value != null
        }

        companion object {
            val INSTANCE = NotNull()
        }
    }
}
