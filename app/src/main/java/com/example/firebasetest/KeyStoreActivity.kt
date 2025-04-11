package com.example.firebasetest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emailauth.R
import com.example.firebasetest.util.AESUtil
import com.example.firebasetest.util.KeyStoreHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import javax.crypto.SecretKey
import kotlin.random.Random

class KeyStoreActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SecItemAdapter
    private val itemList = ArrayList<SecItem>()
    private lateinit var addButton: FloatingActionButton
    private lateinit var deleteAllButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_store) // Ensure you have this layout

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load existing encrypted items (if any) - Replace with your data loading logic
        loadEncryptedItems()

        adapter =
            SecItemAdapter(this, itemList, ::deleteItem, ::editItem, ::containsKeyItem, ::readItem)
        recyclerView.adapter = adapter

        addButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            addNewItem()
        }

        deleteAllButton = findViewById(R.id.deleteAllButton)
        deleteAllButton.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }
    }

    private fun loadEncryptedItems() {
        // In a real app, you would load encrypted data from SharedPreferences, a database, etc.
        // For this demo, we'll start with an empty list.
        // Example of how you might load (replace with your actual implementation):
        // val encryptedDataMap = loadFromStorage()
        // itemList.addAll(encryptedDataMap.map { SecItem(it.key, it.value) })
    }

    private fun addNewItem() {
        val newItemKey = System.currentTimeMillis().toString() // Simulate timestamp key
        val randomValue = generateRandomValue()
//        val encryptedValue = KeyStoreHelper.encryptData(randomValue)
        val key: SecretKey = AESUtil.getKeyGenerator("myKey").generateKey()
        val encryptData = AESUtil.encryptAES(randomValue.toByteArray(), key)
        val newItem = SecItem(newItemKey, encryptData!!)
        itemList.add(newItem)
        adapter.notifyItemInserted(itemList.size - 1)
        // In a real app, you would also save the key and encryptedValue to persistent storage
    }

    private fun deleteItem(position: Int) {
        itemList.removeAt(position)
        adapter.notifyItemRemoved(position)
        // In a real app, you would also remove the key-value pair from persistent storage
    }

    private fun editItem(position: Int, newText: String) {
        itemList[position].updateValue(newText)
        adapter.notifyItemChanged(position)
        // In a real app, you would also update the encrypted value in persistent storage for the given key
    }

    private fun containsKeyItem(key: String) {
        // In a real secure storage implementation, directly checking for the presence
        // of a key might not be exposed or could have security implications.
        // This is a placeholder for demonstration.
        val contains = itemList.any { it.key == key }
        Toast.makeText(this, "Contains Key '$key': $contains", Toast.LENGTH_SHORT).show()
    }

    private fun readItem(key: String) {
        val item = itemList.find { it.key == key }
        val value = item?.decryptedValue ?: "Not found"
        Toast.makeText(this, "Value for key '$key': $value", Toast.LENGTH_LONG).show()
    }

    private fun showDeleteAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("删除所有 Item")
            .setMessage("确定要删除所有 Item 吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteAllItems()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteAllItems() {
        val itemCount = itemList.size
        itemList.clear()
        adapter.notifyItemRangeRemoved(0, itemCount)
        // In a real app, you would also clear all key-value pairs from persistent storage
    }

    private fun generateRandomValue(): String {
        val rand = Random
        val codeUnits = List(20) { rand.nextInt(26) + 65 }
        return String(codeUnits.toIntArray(), 0, codeUnits.size)
    }

    data class SecItem(val key: String, var value: ByteArray) {
        val decryptedValue: String
            get() = KeyStoreHelper.decryptData(value)

        fun updateValue(newValue: String) {
            value = KeyStoreHelper.encryptData(newValue)
        }
    }

    class SecItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    class SecItemAdapter(
        private val mContext: Context,
        private val items: MutableList<SecItem>,
        private val onDelete: (Int) -> Unit,
        private val onEdit: (Int, String) -> Unit,
        private val onContainsKey: (String) -> Unit,
        private val onRead: (String) -> Unit
    ) : RecyclerView.Adapter<SecItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecItemViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return SecItemViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SecItemViewHolder, position: Int) {
            val currentItem = items[position]
            holder.textView.text = currentItem.decryptedValue

            holder.itemView.setOnLongClickListener {
                showItemOptionsDialog(holder.itemView, position)
                true
            }
        }

        private fun showItemOptionsDialog(view: View, position: Int) {
            val options = arrayOf("编辑", "删除", "包含 Key", "读取")
            AlertDialog.Builder(mContext)
                .setTitle("选择操作")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showEditDialog(position)
                        1 -> onDelete(position)
                        2 -> onContainsKey(items[position].key)
                        3 -> onRead(items[position].key)
                    }
                }
                .show()
        }

        private fun showEditDialog(position: Int) {
            val currentItem = items[position]
            val editText = EditText(mContext)
            editText.setText(currentItem.decryptedValue)

            AlertDialog.Builder(mContext)
                .setTitle("编辑 Item")
                .setView(editText)
                .setPositiveButton("保存") { _, _ ->
                    val newText = editText.text.toString().trim()
                    if (newText.isNotEmpty()) {
                        onEdit(position, newText)
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        }

        override fun getItemCount(): Int = items.size
    }
}

//    class EditItemWidget(text: String) : AlertDialog.Builder(null) {
//        private val controller = EditText(context).apply { setText(text) }
//
//        init {
//            setTitle("编辑 Item")
//            setView(controller)
//            setPositiveButton("保存") { dialog, _ ->
//                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)?.tag =
//                    controller.text
//            }
//            setNegativeButton("取消", null)
//        }
//
//        fun getValue(): String? {
//            val dialog = show()
//            return dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.tag as? String
//        }
//    }