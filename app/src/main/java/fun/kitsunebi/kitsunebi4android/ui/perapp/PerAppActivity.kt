package `fun`.kitsunebi.kitsunebi4android.ui.perapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `fun`.kitsunebi.kitsunebi4android.R

class PerAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_per_app)
        supportFragmentManager
                .beginTransaction()
                .add(R.id.content_per_app, PerAppFragment())
                .commit()
    }
}