package org.cobbzilla.util.system

interface CommandProgressCallback {

    /**
     * Called when a CommandProgressFilter encounters a new indicator
     * @param marker Contains the percent done, pattern that matched, and the specific line that matched
     */
    fun updateProgress(marker: CommandProgressMarker)

}
