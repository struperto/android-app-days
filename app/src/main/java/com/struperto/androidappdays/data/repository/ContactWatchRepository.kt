package com.struperto.androidappdays.data.repository

import android.content.Context
import android.provider.ContactsContract

data class ContactSignal(
    val name: String,
    val contactId: String,
    val lastContacted: Long?,
    val timesContacted: Int,
)

interface ContactWatchRepository {
    suspend fun loadRecentContacts(limit: Int = 20): List<ContactSignal>
}

class DeviceContactWatchRepository(
    private val context: Context,
) : ContactWatchRepository {
    @Suppress("DEPRECATION")
    override suspend fun loadRecentContacts(limit: Int): List<ContactSignal> {
        val contacts = mutableListOf<ContactSignal>()
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.LAST_TIME_CONTACTED,
            ContactsContract.Contacts.TIMES_CONTACTED,
        )
        val sortOrder = "${ContactsContract.Contacts.LAST_TIME_CONTACTED} DESC"
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, projection, null, null, sortOrder,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val lastCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LAST_TIME_CONTACTED)
            val timesCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.TIMES_CONTACTED)
            var count = 0
            while (cursor.moveToNext() && count < limit) {
                contacts += ContactSignal(
                    name = cursor.getString(nameCol) ?: "",
                    contactId = cursor.getString(idCol) ?: "",
                    lastContacted = cursor.getLong(lastCol).takeIf { it > 0 },
                    timesContacted = cursor.getInt(timesCol),
                )
                count++
            }
        }
        return contacts
    }
}
