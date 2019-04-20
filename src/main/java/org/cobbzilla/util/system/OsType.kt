package org.cobbzilla.util.system

import com.fasterxml.jackson.annotation.JsonCreator
import com.sun.jna.Platform

import org.cobbzilla.util.daemon.ZillaRuntime.die

enum class OsType {

    windows, macosx, linux;


    companion object {

        @JsonCreator
        fun fromString(`val`: String): OsType {
            return valueOf(`val`.toLowerCase())
        }

        val CURRENT_OS = initCurrentOs()

        private fun initCurrentOs(): OsType {
            if (Platform.isWindows()) return windows
            if (Platform.isMac()) return macosx
            return if (Platform.isLinux()) linux else die("could not determine operating system: " + System.getProperty("os.name"))
        }

        val IS_ADMIN = initIsAdmin()

        private fun initIsAdmin(): Boolean {
            when (CURRENT_OS) {
                macosx, linux -> return System.getProperty("user.name") == "root"
                windows -> return WindowsAdminUtil.isUserWindowsAdmin
                else -> return false
            }
        }

        val ADMIN_USERNAME = initAdminUsername()

        private fun initAdminUsername(): String {
            when (CURRENT_OS) {
                macosx, linux -> return "root"
                windows -> return "Administrator"
                else -> return die("initAdminUsername: invalid OS: $CURRENT_OS")
            }
        }
    }

}
