package com.example.seniorcare

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import java.util.*
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.os.Process
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UsestatActivity : AppCompatActivity() {

    //로그 변수
    private val TAG = UsestatActivity::class.java.simpleName

    companion object {
        const val TAG = "UsestatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usestat)


        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        var fab = findViewById<FloatingActionButton>(R.id.fab)

        fab.setOnClickListener { view ->
            if (!checkForPermission()) {
                Log.e(TAG, "app usage 허용 안함")
                Toast.makeText(
                    this,
                    "화면의 버튼을 클릭하여 접근권한을 허용하세요",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } else {
                Log.e(TAG, "app usage 허용")
                val usageStats = getAppUsageStats()
                showAppUsageStats(usageStats)
            }
        }
    }

    private fun checkForPermission(): Boolean {
        Log.e(TAG, "checkForPermission")
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == MODE_ALLOWED
    }

    private fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        Log.e(TAG, "showAppUsageStats")
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })

        Log.e(TAG, "첫번째 값 : ${usageStats.first().lastTimeUsed}")
        Log.e(TAG, "첫번째 값 : ${Date(usageStats.first().lastTimeUsed)}")


        usageStats.forEach { it ->
            Log.d(TAG, "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, " +
                    "totalTimeInForeground: ${it.totalTimeInForeground}")
        }
    }

    private fun getAppUsageStats(): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager
            .queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis,
                System.currentTimeMillis()
            )
        return queryUsageStats
    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}