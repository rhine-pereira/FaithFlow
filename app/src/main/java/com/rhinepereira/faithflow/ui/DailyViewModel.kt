package com.rhinepereira.faithflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.faithflow.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

/*
class DailyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VerseRepository
    private val dao: VerseDao
    
    private val authRepository = AuthRepository()
    
    val todayRecord: StateFlow<DailyRecord?>
    val allDailyRecords: StateFlow<List<DailyRecord>>

    init {
        val database = AppDatabase.getDatabase(application)
        dao = database.verseDao()
        repository = VerseRepository(application, dao)
        
        val startOfToday = getStartOfDay(System.currentTimeMillis())
        val endOfToday = startOfToday + (24 * 60 * 60 * 1000)
        
        val authStatus = authRepository.authStatusFlow()
        
        todayRecord = authStatus.flatMapLatest { status ->
            when (status) {
                is AuthStatus.Authenticated -> {
                    dao.getRecordForDate(status.userId, startOfToday, endOfToday)
                }
                else -> flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allDailyRecords = authStatus.flatMapLatest { status ->
            when (status) {
                is AuthStatus.Authenticated -> {
                    dao.getAllDailyRecords(status.userId)
                }
                else -> flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        // Initial fetch from cloud after authentication
        viewModelScope.launch {
            authStatus.collect { status ->
                if (status is AuthStatus.Authenticated) {
                    repository.fetchFromSupabase(status.userId)
                }
            }
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun updateDailyRecord(
        readToday: Boolean? = null,
        whatRead: String? = null,
        readTime: Int? = null,
        prayedToday: Boolean? = null,
        prayerTime: Int? = null,
        prophecy: String? = null
    ) {
        viewModelScope.launch {
            val startOfToday = getStartOfDay(System.currentTimeMillis())
            val endOfToday = startOfToday + (24 * 60 * 60 * 1000)
            val userId = authRepository.currentUserId ?: ""
            val existing = dao.getRecordForDateSync(userId, startOfToday, endOfToday) ?: DailyRecord(date = startOfToday, userId = userId)
            
            val updated = existing.copy(
                readToday = readToday ?: existing.readToday,
                whatRead = whatRead ?: existing.whatRead,
                totalReadTimeMinutes = readTime ?: existing.totalReadTimeMinutes,
                prayedToday = prayedToday ?: existing.prayedToday,
                totalPrayerTimeMinutes = prayerTime ?: existing.totalPrayerTimeMinutes,
                prophecy = prophecy ?: existing.prophecy,
                isSynced = false
            )
            
            dao.insertDailyRecord(updated)
            repository.scheduleSync()
        }
    }
}
*/
