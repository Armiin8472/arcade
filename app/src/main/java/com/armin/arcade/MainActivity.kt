package com.armin.arcade

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private data class Item(
        val name: String,
        val emoji: String,
        val desc: String,
        val cat: String,       // "game" | "prog"
        val file: String,      // absolute path (custom) or asset name (builtin)
        val iconRes: Int,      // 0 for custom emoji
        val custom: Boolean
    )

    private val builtins = listOf(
        Item("مار سایبری", "", "مار نئونی با ۳ سرعت", "game", "snake.html", R.drawable.ic_snake, false),
        Item("تتریس نهایی", "", "بلوک‌ها رو مرتب کن", "game", "tetris.html", R.drawable.ic_tetris, false),
        Item("حدس عدد", "", "عدد سه‌رقمی رو پیدا کن", "game", "guess.html", R.drawable.ic_guess, false),
        Item("رمزگذار XOR", "", "رمزنگاری و رمزگشایی متن", "prog", "cipher.html", R.drawable.ic_lock, false)
    )

    private var customs = mutableListOf<Item>()
    private lateinit var llList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        llList = findViewById(R.id.ll_list)
        loadCustoms()
        render()

        findViewById<View>(R.id.fab_add).setOnClickListener { showAddDialog() }
    }

    override fun onResume() {
        super.onResume()
        loadCustoms()
        render()
    }

    // ---------- rendering with sections ----------
    private fun render() {
        llList.removeAllViews()

        val games = builtins.filter { it.cat == "game" } + customs.filter { it.cat == "game" }
        val progs = builtins.filter { it.cat == "prog" } + customs.filter { it.cat == "prog" }

        if (games.isNotEmpty()) {
            addHeader("🎮 بازی‌ها")
            games.forEach { addCard(it) }
        }
        if (progs.isNotEmpty()) {
            addHeader("📱 برنامه‌ها")
            progs.forEach { addCard(it) }
        }
    }

    private fun addHeader(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.setTextColor(0xFF8A93A6.toInt())
        tv.textSize = 14f
        tv.setPadding(dp(4), dp(18), dp(4), dp(6))
        llList.addView(tv, matchWrap())
    }

    private fun addCard(item: Item) {
        val card = layoutInflater.inflate(R.layout.card_game, llList, false)
        val emoji = card.findViewById<TextView>(R.id.tv_emoji)
        val icon = card.findViewById<ImageView>(R.id.iv_icon)
        card.findViewById<TextView>(R.id.tv_title).text = item.name
        card.findViewById<TextView>(R.id.tv_desc).text = item.desc

        if (item.iconRes != 0) {
            icon.setImageResource(item.iconRes)
            icon.visibility = View.VISIBLE
            emoji.visibility = View.GONE
        } else {
            emoji.text = item.emoji.ifBlank { "🌐" }
            emoji.visibility = View.VISIBLE
            icon.visibility = View.GONE
        }

        card.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java).apply {
                if (item.custom) putExtra("path", item.file) else putExtra("file", item.file)
                putExtra("title", item.name)
            })
        }
        if (item.custom) {
            card.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setTitle(item.name)
                    .setMessage("حذف این مورد؟")
                    .setPositiveButton("حذف") { _, _ ->
                        File(item.file).delete()
                        customs.removeAll { it.file == item.file }
                        saveCustoms()
                        render()
                    }
                    .setNegativeButton("انصراف", null)
                    .show()
                true
            }
        }
        llList.addView(card, matchWrap())
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    // ---------- add dialog ----------
    private var pickedUri: Uri? = null

    private fun showAddDialog() {
        pickedUri = null
        val view = layoutInflater.inflate(R.layout.dialog_add, null)
        val etName = view.findViewById<EditText>(R.id.et_name)
        val etEmoji = view.findViewById<EditText>(R.id.et_emoji)
        val etDesc = view.findViewById<EditText>(R.id.et_desc)
        val chipGame = view.findViewById<TextView>(R.id.chip_game)
        val chipProg = view.findViewById<TextView>(R.id.chip_prog)
        val btnPick = view.findViewById<TextView>(R.id.btn_pick)
        val tvFile = view.findViewById<TextView>(R.id.tv_file)

        var cat = "game"
        fun paint() {
            chipGame.alpha = if (cat == "game") 1f else 0.45f
            chipProg.alpha = if (cat == "prog") 1f else 0.45f
        }
        paint()
        chipGame.setOnClickListener { cat = "game"; paint() }
        chipProg.setOnClickListener { cat = "prog"; paint() }

        btnPick.setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/html", "text/plain", "application/octet-stream"))
            startActivityForResult(i, 1001)
        }
        // remember views for onActivityResult
        pendingTvFile = tvFile

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("افزودن") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { toast("اسم رو بنویس"); return@setPositiveButton }
                val uri = pickedUri
                if (uri == null) { toast("اول فایل HTML رو انتخاب کن"); return@setPositiveButton }
                val dest = File(filesDir, "custom_${System.currentTimeMillis()}.html")
                try {
                    contentResolver.openInputStream(uri)!!.use { ins ->
                        dest.outputStream().use { out -> ins.copyTo(out) }
                    }
                } catch (e: Exception) {
                    toast("کپی فایل شکست خورد"); return@setPositiveButton
                }
                customs.add(
                    Item(name, etEmoji.text.toString().trim(), etDesc.text.toString().trim(),
                        cat, dest.absolutePath, 0, true)
                )
                saveCustoms()
                render()
                toast("اضافه شد ✅")
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private var pendingTvFile: TextView? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data?.data != null) {
            pickedUri = data.data
            val name = queryName(pickedUri!!)
            pendingTvFile?.text = "📄 $name"
            pendingTvFile?.setTextColor(0xFF16A34A.toInt())
        }
    }

    private fun queryName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) {
                val i = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (i >= 0) return it.getString(i)
            }
        }
        return uri.lastPathSegment ?: "فایل"
    }

    // ---------- persistence ----------
    private fun loadCustoms() {
        customs.clear()
        val raw = getSharedPreferences("arcade", MODE_PRIVATE).getString("items", null) ?: return
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val f = File(o.getString("file"))
                if (!f.exists()) continue
                customs.add(
                    Item(o.getString("name"), o.optString("emoji", ""), o.optString("desc", ""),
                        o.optString("cat", "game"), f.absolutePath, 0, true)
                )
            }
        } catch (_: Exception) {}
    }

    private fun saveCustoms() {
        val arr = JSONArray()
        customs.forEach { c ->
            arr.put(JSONObject().apply {
                put("name", c.name); put("emoji", c.emoji); put("desc", c.desc)
                put("cat", c.cat); put("file", c.file)
            })
        }
        getSharedPreferences("arcade", MODE_PRIVATE).edit().putString("items", arr.toString()).apply()
    }

    private fun toast(m: String) =
        android.widget.Toast.makeText(this, m, android.widget.Toast.LENGTH_SHORT).show()
}
