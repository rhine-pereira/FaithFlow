package com.rhinepereira.faithflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.faithflow.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DailyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VerseRepository
    private val dao: VerseDao
    private val authRepository = AuthRepository()
    
    // State for the currently viewed date
    private val _targetDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val targetDate: StateFlow<Long> = _targetDate.asStateFlow()

    private val _isSealing = MutableStateFlow(false)
    val isSealing: StateFlow<Boolean> = _isSealing.asStateFlow()

    val currentRecord: StateFlow<DailyRecord?>
    val allDailyRecords: StateFlow<List<DailyRecord>>

    init {
        val database = AppDatabase.getDatabase(application)
        dao = database.verseDao()
        repository = VerseRepository(application, dao)
        
        val authStatus = authRepository.authStatusFlow()
        
        // Fetch record whenever targetDate or authStatus changes
        currentRecord = combine(authStatus, _targetDate) { status, date ->
            Pair(status, date)
        }.flatMapLatest { (status, date) ->
            when (status) {
                is AuthStatus.Authenticated -> {
                    val endOfDay = date + (24 * 60 * 60 * 1000)
                    dao.getRecordForDate(status.userId, date, endOfDay)
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
        
        // Initial fetch from cloud
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

    fun setTargetDate(timestamp: Long) {
        _targetDate.value = getStartOfDay(timestamp)
    }

    fun moveDate(days: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _targetDate.value
        calendar.add(Calendar.DAY_OF_YEAR, days)
        
        val newDate = getStartOfDay(calendar.timeInMillis)
        val today = getStartOfDay(System.currentTimeMillis())
        
        if (newDate <= today) {
            _targetDate.value = newDate
        }
    }

    private suspend fun updateDailyRecordInternal(
        readToday: Boolean? = null,
        whatRead: String? = null,
        readTime: Int? = null,
        prayedToday: Boolean? = null,
        prayerTime: Int? = null,
        prophecy: String? = null,
        isSealed: Boolean? = null
    ) {
        val date = _targetDate.value
        val endOfDay = date + (24 * 60 * 60 * 1000)
        val userId = authRepository.currentUserId ?: ""
        if (userId.isEmpty()) return

        val existing = dao.getRecordForDateSync(userId, date, endOfDay) ?: DailyRecord(date = date, userId = userId)
        
        // Apply updates
        var updated = existing.copy(
            readToday = readToday ?: existing.readToday,
            whatRead = whatRead ?: existing.whatRead,
            totalReadTimeMinutes = readTime ?: existing.totalReadTimeMinutes,
            prayedToday = prayedToday ?: existing.prayedToday,
            totalPrayerTimeMinutes = prayerTime ?: existing.totalPrayerTimeMinutes,
            prophecy = prophecy ?: existing.prophecy,
            isSealed = isSealed ?: existing.isSealed,
            isSynced = false,
            userId = userId
        )

        // Force clearing when toggled to No based on the resulting updated state
        if (!updated.readToday) {
            updated = updated.copy(whatRead = "", totalReadTimeMinutes = 0)
        }
        if (!updated.prayedToday) {
            updated = updated.copy(totalPrayerTimeMinutes = 0)
        }
        
        dao.insertDailyRecord(updated)
    }

    fun updateDailyRecord(
        readToday: Boolean? = null,
        whatRead: String? = null,
        readTime: Int? = null,
        prayedToday: Boolean? = null,
        prayerTime: Int? = null,
        prophecy: String? = null,
        isSealed: Boolean? = null
    ) {
        viewModelScope.launch {
            updateDailyRecordInternal(
                readToday, whatRead, readTime, prayedToday, prayerTime, prophecy, isSealed
            )
        }
    }

    fun sealTodayWalk() {
        viewModelScope.launch {
            _isSealing.value = true
            updateDailyRecordInternal(isSealed = true)
            repository.scheduleSync() // Cloud sync triggered only on Seal
            kotlinx.coroutines.delay(1500) // Visual duration for feedback
            _isSealing.value = false
        }
    }
}
