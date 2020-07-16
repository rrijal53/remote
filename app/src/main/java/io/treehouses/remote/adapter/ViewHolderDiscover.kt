package io.treehouses.remote.adapter

import io.treehouses.remote.Fragments.DiscoverFragment

import android.content.Context
import android.view.View
import android.widget.TextView
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderDiscover internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private val discoverText: TextView = v.findViewById(R.id.discover_text)
    private var readMessage  = ""
    //listener

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
//    val mHandler: Handler = @SuppressLint("HandlerLeak")
//    object : Handler() {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                Constants.MESSAGE_READ -> {
//                    readMessage = msg.obj as String
//                    Log.d(TAG, "readMessage = $readMessage")
//                    updateSelection(readMessage)
//                }
//            }
//        }
//    }

    init {
        discoverText.setOnClickListener( {
            listener.openCallFragment(DiscoverFragment())
        })
//        mChatService = listener.getChatService()
//        mChatService.updateHandler(mHandler)
//        listener.sendMessage("treehouses blocker")
//
//        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, i: Int ->
//            when (i) {
//                R.id.radioButton1 -> {
//                    listener.sendMessage("treehouses blocker 0")
//                    context.toast("Blocker Disabled")
//                }
//                R.id.radioButton2 -> {
//                    listener.sendMessage("treehouses blocker 1")
//                    context.toast("Blocker set to level 1")
//                }
//                R.id.radioButton3 -> {
//                    listener.sendMessage("treehouses blocker 2")
//                    context.toast("Blocker set to level 2")
//                }
//                R.id.radioButton4 -> {
//                    listener.sendMessage("treehouses blocker 3")
//                    context.toast("Blocker set to level 3")
//                }
//                R.id.radioButton5 -> {
//                    listener.sendMessage("treehouses blocker 4")
//                    context.toast("Blocker set to level 4")
//                }
//                R.id.radioButton6 -> {
//                    listener.sendMessage("treehouses blocker max")
//                    context.toast("Blocker set to maximum level")
//                }
//            }
//        }
    }

//    fun updateSelection(readMessage:String){
//        if(readMessage.contains("blocker 0")){
//            radioGroup.check(R.id.radioButton1);
//        }
//        else if(readMessage.contains("blocker 1")){
//            radioGroup.check(R.id.radioButton2);
//        }
//        else if(readMessage.contains("blocker 2")){
//            radioGroup.check(R.id.radioButton3);
//        }
//        else if(readMessage.contains("blocker 3")){
//            radioGroup.check(R.id.radioButton4);
//        }
//        else if(readMessage.contains("blocker 4")){
//            radioGroup.check(R.id.radioButton5);
//        }
//        else if(readMessage.contains("blocker X")){
//            radioGroup.check(R.id.radioButton6);
//        }
//    }
//
//    companion object {
//        private const val TAG = "ViewHolderBlocker"
//    }
//
//    fun Context?.toast(s: String): Toast {
//        return Toast.makeText(this, s, Toast.LENGTH_SHORT).apply { show() }
//    }


}