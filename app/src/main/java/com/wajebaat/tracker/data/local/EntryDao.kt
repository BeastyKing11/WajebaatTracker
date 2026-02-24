package com.wajebaat.tracker.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntries(): LiveData<List<Entry>>

    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesSync(): List<Entry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: Entry): Long

    @Update
    suspend fun updateEntry(entry: Entry)

    @Delete
    suspend fun deleteEntry(entry: Entry)

    @Query("DELETE FROM entries")
    suspend fun deleteAllEntries()

    @Query("SELECT SUM(amount) FROM entries")
    fun getTotalAmount(): LiveData<Double?>

    @Query("SELECT SUM(wajebaat) FROM entries")
    fun getTotalWajebaat(): LiveData<Double?>

    @Query("SELECT SUM(moneyLeft) FROM entries")
    fun getTotalMoneyLeft(): LiveData<Double?>

    @Query("SELECT COUNT(*) FROM entries")
    fun getEntryCount(): LiveData<Int>
}
