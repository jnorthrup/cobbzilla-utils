package org.cobbzilla.util.system

import com.sun.jna.LastErrorException
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.win32.StdCallLibrary

object WindowsAdminUtil {

    val INSTANCE: Shell32? = if (Platform.isWindows())
        Native.loadLibrary("shell32", Shell32::class.java) as Shell32
    else
        null

    val isUserWindowsAdmin: Boolean
        get() = INSTANCE != null && INSTANCE.IsUserAnAdmin()

    interface Shell32 : StdCallLibrary {
        @Throws(LastErrorException::class)
        fun IsUserAnAdmin(): Boolean
    }
}