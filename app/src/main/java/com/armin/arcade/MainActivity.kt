package com.armin.arcade

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private data class Game(val title: String, val emoji: String, val desc: String, val file: String, val color: String)

    private val games = listOf(
        Game("مار سایبری", "🐍", "مار نئونی با ۳ سرعت", "snake.html", "#00ffff"),
        Game("تتریس نهایی", "🧱", "بلوک‌ها رو مرتب کن", "tetris.html", "#9f45ff"),
        Game("حدس عدد", "🎯", "عدد سه‌رقمی رو پیدا کن", "guess.html", "#60a5fa"),
        Game("رمزگذار XOR", "🔒", "رمزنگاری و رمزگشایی متن", "cipher.html", "#22c55e")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ll = findViewById<android.widget.LinearLayout>(R.id.ll_games)

        val pad = (16 * resources.displayMetrics.density).toInt()
        games.forEach { g ->
            val card = layoutInflater.inflate(R.layout.card_game, ll, false)
            card.findViewById<TextView>(R.id.tv_emoji).text = g.emoji
            card.findViewById<TextView>(R.id.tv_title).text = g.title
            card.findViewById<TextView>(R.id.tv_desc).text = g.desc
            card.setOnClickListener {
                startActivity(Intent(this, GameActivity::class.java).apply {
                    putExtra("file", g.file)
                    putExtra("title", g.title)
                })
            }
            ll.addView(card, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = pad })
        }
    }

    private fun toast(m: String) =
        Toast.makeText(this, m, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.TOP, 0, 120)
        }.show()
}
