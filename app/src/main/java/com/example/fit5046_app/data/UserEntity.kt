import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // from GoogleSignInAccount.id
    val displayName: String?,
    val email: String?,
)