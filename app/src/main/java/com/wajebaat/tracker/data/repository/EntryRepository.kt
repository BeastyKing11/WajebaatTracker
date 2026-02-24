package com.wajebaat.tracker.data.repository

import androidx.lifecycle.LiveData
import com.wajebaat.tracker.data.local.AppDatabase
import com.wajebaat.tracker.data.local.Entry
import com.wajebaat.tracker.data.local.EntryDao

class EntryRepository(private val entryDao: EntryDao) {

    val allEntries: LiveData<List<Entry>> = entryDao.getAllEntries()
    val totalAmount: LiveData<Double?> = entryDao.getTotalAmount()
    val totalWajebaat: LiveData<Double?> = entryDao.getTotalWajebaat()
    val totalMoneyLeft: LiveData<Double?> = entryDao.getTotalMoneyLeft()
    val entryCount: LiveData<Int> = entryDao.getEntryCount()

    suspend fun insert(entry: Entry): Long = entryDao.insertEntry(entry)

    suspend fun update(entry: Entry) = entryDao.updateEntry(entry)

    suspend fun delete(entry: Entry) = entryDao.deleteEntry(entry)

    suspend fun deleteAll() = entryDao.deleteAllEntries()

    suspend fun getAllEntriesSync(): List<Entry> = entryDao.getAllEntriesSync()
}
