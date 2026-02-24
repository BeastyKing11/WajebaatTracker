package com.wajebaat.tracker.ui

import android.app.Application
import androidx.lifecycle.*
import com.wajebaat.tracker.data.local.AppDatabase
import com.wajebaat.tracker.data.local.Entry
import com.wajebaat.tracker.data.repository.EntryRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EntryRepository
    val allEntries: LiveData<List<Entry>>
    val totalAmount: LiveData<Double?>
    val totalWajebaat: LiveData<Double?>
    val totalMoneyLeft: LiveData<Double?>
    val entryCount: LiveData<Int>

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    init {
        val dao = AppDatabase.getDatabase(application).entryDao()
        repository = EntryRepository(dao)
        allEntries = repository.allEntries
        totalAmount = repository.totalAmount
        totalWajebaat = repository.totalWajebaat
        totalMoneyLeft = repository.totalMoneyLeft
        entryCount = repository.entryCount
    }

    fun addEntry(amount: Double) {
        val wajebaat = amount / 5.0
        val moneyLeft = amount - wajebaat
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(Date())
        val timestamp = System.currentTimeMillis()

        val entry = Entry(
            amount = amount,
            wajebaat = wajebaat,
            moneyLeft = moneyLeft,
            date = date,
            timestamp = timestamp
        )
        viewModelScope.launch {
            repository.insert(entry)
            _saveSuccess.postValue(true)
        }
    }

    fun updateEntry(entry: Entry, newAmount: Double) {
        val wajebaat = newAmount / 5.0
        val moneyLeft = newAmount - wajebaat
        val updatedEntry = entry.copy(
            amount = newAmount,
            wajebaat = wajebaat,
            moneyLeft = moneyLeft
        )
        viewModelScope.launch {
            repository.update(updatedEntry)
        }
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun deleteAllEntries() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    suspend fun getAllEntriesSync(): List<Entry> = repository.getAllEntriesSync()

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}

class EntryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
