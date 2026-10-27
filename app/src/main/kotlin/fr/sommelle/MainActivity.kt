package fr.sommelle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.sommelle.core.designsystem.SommelleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as SommelleApplication).repository
        setContent {
            SommelleTheme {
                SommelleApp(repository)
            }
        }
    }
}
