package app.cortena.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import framework.cortena.ui.components.Text
import framework.cortena.ui.layout.Body
import framework.cortena.ui.layout.ContentView
import framework.cortena.ui.layout.SafeArea

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContentView {
            Body {
                SafeArea {
                    Text("Calculator")
                }
            }
        }
    }
}
