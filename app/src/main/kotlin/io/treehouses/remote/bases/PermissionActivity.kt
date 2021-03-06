package io.treehouses.remote.bases

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.treehouses.remote.R

abstract class PermissionActivity : AppCompatActivity() {
    private fun checkPermission(strPermission: String?): Boolean {
        val result = ContextCompat.checkSelfPermission(this, strPermission!!)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun statusCheck() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomAlertDialogStyle))
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alert = builder.create()
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.show()
    }

    fun requestPermission() {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || !checkPermission(Manifest.permission.CHANGE_WIFI_STATE)) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_REQUEST_WIFI)
        } else {
            statusCheck()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_WIFI) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                statusCheck()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_WIFI = 111
    }
}