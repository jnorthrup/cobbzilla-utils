package org.cobbzilla.util.json.main

import org.cobbzilla.util.io.FileUtil
import org.cobbzilla.util.json.JsonEdit
import org.cobbzilla.util.json.JsonEditOperation
import org.cobbzilla.util.main.BaseMain

import org.cobbzilla.util.daemon.ZillaRuntime.empty

class JsonEditor : BaseMain<JsonEditorOptions>() {

    @Throws(Exception::class)
    public override fun run() {
        val options = options
        val edit = JsonEdit()
                .setJsonData(options!!.inputJson)
                .addOperation(JsonEditOperation()
                        .setType(options.operationType)
                        .setPath(options.path)
                        .setJson(options.value))

        val json = edit.edit()

        if (options.hasOutfile()) {
            FileUtil.toFile(options.outfile, json)
        } else {
            if (empty(json)) {
                System.exit(1)
            } else {
                print(json)
            }
        }
        System.exit(0)
    }

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(JsonEditor::class.java, args)
        }
    }

}