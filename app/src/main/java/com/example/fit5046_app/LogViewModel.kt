import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    init {
        repository = LogRepository(application)
    }

    fun insertDietLog(dietLog: DietLogEntity) = viewModelScope.launch(Dispatchers.IO) {
        println("Inserting diet log: $dietLog")
        repository.insertDietLog(dietLog)
    }

    fun insertBloodGlucoseLog(bloodGlucoseLog: BloodGlucoseLogEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBloodGlucoseLog(bloodGlucoseLog)
        }

}