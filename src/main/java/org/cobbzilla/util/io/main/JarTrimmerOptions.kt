package org.cobbzilla.util.io.main

import org.cobbzilla.util.io.FileUtil
import org.cobbzilla.util.io.JarTrimmerConfig
import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Option

import java.io.File

import org.cobbzilla.util.json.JsonUtil.json

class JarTrimmerOptions : BaseMainOptions() {
    @Option(name = OPT_CONFIG, aliases = [LONGOPT_CONFIG], usage = USAGE_CONFIG)
    var configFile: File? = null

    val config: JarTrimmerConfig?
        get() = json(FileUtil.toStringOrDie(configFile), JarTrimmerConfig::class.java)

    companion object {

        val USAGE_CONFIG = "JSON configuration file to drive JarTrimmer"
        val OPT_CONFIG = "-c"
        val LONGOPT_CONFIG = "--config"
    }
}
