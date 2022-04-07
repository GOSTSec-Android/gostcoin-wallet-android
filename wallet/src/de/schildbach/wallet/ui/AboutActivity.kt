/*
 * Copyright 2019 Dash Core Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schildbach.wallet.ui

import android.content.*
import android.content.Intent.ACTION_VIEW
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.installations.FirebaseInstallations
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import dagger.hilt.android.AndroidEntryPoint
import org.dash.wallet.common.services.analytics.AnalyticsConstants
import org.dash.wallet.features.exploredash.repository.ExploreRepository
import java.lang.Exception
import javax.inject.Inject
import de.schildbach.wallet.WalletApplication
import de.schildbach.wallet_test.BuildConfig
import de.schildbach.wallet_test.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.coroutines.launch
import org.bitcoinj.core.VersionMessage
import org.slf4j.LoggerFactory
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class AboutActivity : BaseMenuActivity(), SensorEventListener {
    // variables for shake detection
    private val SHAKE_THRESHOLD = 1.50f // m/S**2

    private val MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000
    private var mLastShakeTime: Long = 0
    private lateinit var mSensorMgr: SensorManager

    companion object {
        private val log = LoggerFactory.getLogger(AboutActivity::class.java)
    }

    @Inject
    lateinit var exploreRepository: ExploreRepository


    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.about_title)
        app_version_name.text = getString(R.string.about_version_name, BuildConfig.VERSION_NAME)
        app_version_code.text = getString(R.string.about_version_extra, BuildConfig.VERSION_CODE, BuildConfig.FLAVOR)
        library_version_name.text = getString(R.string.about_credits_bitcoinj_title,
                VersionMessage.BITCOINJ_VERSION)

        github_link.setOnClickListener {
            val i = Intent(ACTION_VIEW)
            i.data = Uri.parse(github_link.text.toString())
            startActivity(i)
        }
        review_and_rate.setOnClickListener { openReviewAppIntent() }
        contact_support.setOnClickListener {
            analytics.logEvent(AnalyticsConstants.Settings.ABOUT_SUPPORT, bundleOf())
            handleReportIssue()
        }

        showFirebaseInstallationId()
        // Get a sensor manager to listen for shakes
        mSensorMgr = getSystemService(SENSOR_SERVICE) as SensorManager
        showExploreDashSyncStatus()
    }

    private fun showFirebaseInstallationId() {
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            firebase_installation_id.isVisible = task.isSuccessful
            if (task.isSuccessful) {
                firebase_installation_id.text = task.result
            }
            firebase_installation_id.setCopyable("Firebase Installation ID")
        }
    }

    private fun showExploreDashSyncStatus() {
        lifecycleScope.launch {
            val formatFlags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME

            val formattedUpdateTime = try {
                val timestamp = exploreRepository.getRemoteTimestamp()
                DateUtils.formatDateTime(applicationContext, timestamp, formatFlags)
            } catch (ex: Exception) {
                getString(R.string.about_last_explore_dash_update_error)
            }

            val lastSync = exploreRepository.localTimestamp
            val formattedSyncTime = if (lastSync == 0L) {
                getString(R.string.about_last_explore_dash_sync_never)
            } else {
                DateUtils.formatDateTime(applicationContext, lastSync, formatFlags)
            }

            explore_dash_last_sync.text = getString(R.string.about_last_explore_dash_sync, formattedUpdateTime, formattedSyncTime)
            explore_dash_last_sync.setCopyable("Explore Dash last sync")
        }
    }

    override fun onResume() {
        super.onResume()
        // Listen for shakes
        val accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // stop listening for shakes
        val accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            mSensorMgr.unregisterListener(this)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_stay, R.anim.slide_out_left)
    }

    private fun openReviewAppIntent() {
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // and go back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun handleReportIssue() {
        alertDialog = ReportIssueDialogBuilder.createReportIssueDialog(this,
                WalletApplication.getInstance()).buildAlertDialog()
        alertDialog.show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if (curTime - mLastShakeTime > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = sqrt(x.toDouble().pow(2.0) +
                        y.toDouble().pow(2.0) +
                        z.toDouble().pow(2.0)) - SensorManager.GRAVITY_EARTH

                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime
                    log.info("Shake detected: developer mode changing to ${!configuration.developerMode}")
                    configuration.developerMode = if (!configuration.developerMode) {
                        Toast.makeText(this, R.string.about_developer_mode, LENGTH_LONG).show()
                        true
                    } else {
                        Toast.makeText(this, R.string.about_developer_mode_disabled, LENGTH_LONG).show()
                        false
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignore
    }

    private fun TextView.setCopyable(label: String) {
        this.setOnClickListener {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).run {
                setPrimaryClip(ClipData.newPlainText(label, this@setCopyable.text))
            }
            Toast.makeText(this@AboutActivity, "Copied", LENGTH_LONG).show()
        }
    }
}