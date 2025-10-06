import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordingDao {
    @Insert
    suspend fun insert(recording: Recording)

    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    suspend fun getAll(): List<Recording>
}
