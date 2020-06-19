package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.adapter.ServiceCardAdapter
import io.treehouses.remote.adapter.ServicesListAdapter
import io.treehouses.remote.bases.BaseServicesFragment
import io.treehouses.remote.callback.ServiceAction
import io.treehouses.remote.databinding.ActivityServicesDetailsBinding
import io.treehouses.remote.databinding.DialogChooseUrlBinding
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServicesDetailsFragment internal constructor(private val services: ArrayList<ServiceInfo>) : BaseServicesFragment(), OnItemSelectedListener, OnPageChangeListener, ServiceAction {
    private var received = false
    private var wait = false
    private var spinnerAdapter: ServicesListAdapter? = null
    private var selected: ServiceInfo? = null
    private var serviceCardAdapter: ServiceCardAdapter? = null
    private var scrolled = false
    private var binding: ActivityServicesDetailsBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.chatService
        binding = ActivityServicesDetailsBinding.inflate(inflater, container, false)
        spinnerAdapter = ServicesListAdapter(context, services, resources.getColor(R.color.md_grey_600))
        binding!!.pickService.adapter = spinnerAdapter
        binding!!.pickService.setSelection(1)
        binding!!.pickService.onItemSelectedListener = this
        serviceCardAdapter = ServiceCardAdapter(childFragmentManager, services)
        binding!!.servicesCards.adapter = serviceCardAdapter
        binding!!.servicesCards.addOnPageChangeListener(this)
        return binding!!.root
    }

    @JvmField
    val handlerDetails: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val output = msg.obj as String
                    moreActions(output)
                }
            }
        }
    }

    private fun matchOutput(s: String) {
        selected = binding!!.pickService.selectedItem as ServiceInfo
        Log.d("Entered", "matchOutput: $s")
        if (s.contains("started")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_RUNNING
        } else if (s.contains("stopped and removed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_AVAILABLE
            Log.d("STOP", "matchOutput: ")
        } else if (s.contains("stopped") || s.contains("installed")) {
            selected!!.serviceStatus = ServiceInfo.SERVICE_INSTALLED
        } else {
            return
        }
        Collections.sort(services)
        serviceCardAdapter!!.notifyDataSetChanged()
        spinnerAdapter!!.notifyDataSetChanged()
        setScreenState(true)
        wait = false
        goToSelected()
    }

    private fun moreActions(output: String) {
        if (wait) {
            matchOutput(output.trim { it <= ' ' })
        } else if (isLocalUrl(output, received) || isTorURL(output, received)) {
            received = true
            openLocalURL(output.trim { it <= ' ' })
            binding!!.progressBar.visibility = View.GONE
        } else {
            setScreenState(true)
            var msg:String = ""
            if (output.contains("service autorun set")) {
                msg = "Switched autorun"
            } else if (output.toLowerCase().contains("error")) {
                msg = "An Error occurred"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        if (!scrolled) {
            val statusCode = services[position].serviceStatus
            if (statusCode == ServiceInfo.SERVICE_HEADER_AVAILABLE || statusCode == ServiceInfo.SERVICE_HEADER_INSTALLED) return
            val count = countHeadersBefore(position)
            binding!!.servicesCards.currentItem = position - count
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    fun setSelected(s: ServiceInfo) {
        Log.d("SELECTED", "setSelected: " + s.name)
        selected = s
    }

    private fun goToSelected() {
        if (selected != null && binding!!.pickService != null) {
            val pos = inServiceList(selected!!.name, services)
            val count = countHeadersBefore(pos)
            binding!!.servicesCards.currentItem = pos - count
            binding!!.pickService.setSelection(pos)
        }
    }

    override fun onResume() {
        super.onResume()
        goToSelected()
    }

    override fun onPause() {
        super.onPause()
        selected = binding!!.pickService.selectedItem as ServiceInfo
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        Log.d("SELECTED", "onPageSelected: ")
        scrolled = true
        val pos = position + countHeadersBefore(position + 1)
        binding!!.pickService.setSelection(pos)
        scrolled = false
    }

    override fun onPageScrollStateChanged(state: Int) {}
    private fun countHeadersBefore(position: Int): Int {
        var count = 0
        for (i in 0..position) {
            if (services[i].isHeader) count++
        }
        return count
    }

    private fun showDeleteDialog(selected: ServiceInfo?) {
        AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                .setTitle("Delete " + selected!!.name + "?")
                .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
                .setPositiveButton("Delete") { dialog: DialogInterface?, which: Int ->
                    performService("Uninstalling", """treehouses services ${selected.name} cleanup
""", selected.name)
                    performServiceWait()
                }.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.create().show()
    }

    private fun onInstall(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_AVAILABLE) {
            performService("Installing", """treehouses services ${selected.name} install
""", selected.name)
            performServiceWait()
        } else if (installedOrRunning(selected)) {
            showDeleteDialog(selected)
        }
    }

    private fun performServiceWait(){
        wait = true
        setScreenState(false)
    }

    private fun onStart(selected: ServiceInfo?) {
        if (selected!!.serviceStatus == ServiceInfo.SERVICE_INSTALLED) {
            performService("Starting", """treehouses services ${selected.name} up
""", selected.name)
        } else if (selected.serviceStatus == ServiceInfo.SERVICE_RUNNING) {
            performService("Stopping", """treehouses services ${selected.name} stop
""", selected.name)
        }
    }

    private fun setOnClick(v: View, command: String, alertDialog: AlertDialog) {
        v.setOnClickListener { v1: View? ->
            writeToRPI(command)
            alertDialog.dismiss()
            binding!!.progressBar.visibility = View.VISIBLE
        }
    }

    private fun onLink(selected: ServiceInfo?) {
        //reqUrls();
        val chooseBind = DialogChooseUrlBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setView(chooseBind.root).setTitle("Select URL type").create()
        setOnClick(chooseBind.localButton, """treehouses services ${selected!!.name} url local
""", alertDialog)
        setOnClick(chooseBind.torButton, """treehouses services ${selected.name} url tor
""", alertDialog)
        alertDialog.show()
    }

    private fun setScreenState(state: Boolean) {
        binding!!.servicesCards.setPagingEnabled(state)
        binding!!.pickService.isEnabled = state
        if (state) binding!!.progressBar.visibility = View.GONE else binding!!.progressBar.visibility = View.VISIBLE
    }

    override fun onClickInstall(s: ServiceInfo?) {
        onInstall(s)
    }

    override fun onClickStart(s: ServiceInfo?) {
        onStart(s)
        wait = true
        setScreenState(false)
    }

    override fun onClickLink(s: ServiceInfo?) {
        onLink(s)
        received = false
    }

    override fun onClickAutorun(s: ServiceInfo?, newAutoRun: Boolean) {
        setScreenState(false)
        if (newAutoRun) listener.sendMessage("""treehouses services ${s!!.name} autorun true
""") else listener.sendMessage("""treehouses services ${s!!.name} autorun false
""")
        Toast.makeText(context, "Switching autorun status to $newAutoRun", Toast.LENGTH_SHORT).show()
    }

}