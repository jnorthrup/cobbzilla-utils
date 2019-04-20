package org.cobbzilla.util.dns

interface DnsManager {

    /**
     * List matching DNS records
     * @param match The DnsRecordMatch query
     * @return a List of DnsRecords that match
     */
    @Throws(Exception::class)
    fun list(match: DnsRecordMatch): List<DnsRecord>

    /**
     * Write a DNS record
     * @param record a DNS record to create or update
     * @return true if the record was written, false if it was not (it may have been unchanged)
     */
    @Throws(Exception::class)
    fun write(record: DnsRecord): Boolean

    /**
     * Publish changes to DNS records. Must be called after calling write if you want to see the changes publicly.
     */
    @Throws(Exception::class)
    fun publish()

    /**
     * Delete matching DNS records
     * @param match The DnsRecordMatch query
     * @return A count of the number of records deleted, or -1 if this DnsManager does not support returning counts
     */
    @Throws(Exception::class)
    fun remove(match: DnsRecordMatch): Int

}
