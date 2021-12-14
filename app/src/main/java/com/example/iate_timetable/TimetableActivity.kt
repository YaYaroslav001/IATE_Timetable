package com.example.iate_timetable

import android.animation.ValueAnimator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.ActionBar
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import kotlin.system.exitProcess
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.opengl.Visibility
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import androidx.core.view.setPadding
import org.jsoup.select.Elements


class TimetableActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        fun showTimetable (data: String, weekChange: Boolean) {
            try {
                val doc: Document = Jsoup.parse(data)
                val stringBuilder = StringBuilder()
                val element: Elements = doc.select("div[class=card rounded-0 day-of-week]")
                val radioEven: RadioButton = findViewById(R.id.radioEven)
                var evenOdd = 1
                val linearLayout: LinearLayout = findViewById(R.id.timetable_layout)
                linearLayout.removeAllViews()
                if(radioEven.isChecked)
                    evenOdd = 2
                for (i in 0 until element.size) {
                    stringBuilder.clear()
                    val outerCardView = CardView(this)
                    val innerCardView = CardView(this)
                    val titleCardView = CardView(this)
                    val dataView = TextView(this)
                    val titleView = TextView(this)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    var containsEvery = false
                    var containsEven = false
                    var containsOdd = false
                    layoutParams.setMargins(20, 20, 20, 20)
                    outerCardView.layoutParams = layoutParams
                    outerCardView.radius = 42F
                    outerCardView.elevation = 20F
                    innerCardView.layoutParams = layoutParams
                    innerCardView.radius = 27F
                    dataView.layoutParams = layoutParams
                    layoutParams.setMargins(0, 0, 0, 0)
                    titleCardView.layoutParams = layoutParams
                    titleCardView.radius = 17F
                    outerCardView.setCardBackgroundColor(Color.parseColor("#0068B2"))
                    innerCardView.setCardBackgroundColor(Color.parseColor("#E6EFF6"))
                    titleCardView.setCardBackgroundColor(Color.parseColor("#0068B2"))
                    dataView.setBackgroundColor(Color.parseColor("#E6EFF6"))
                    layoutParams.setMargins(20, 20, 20, 20)
                    dataView.setTextColor(Color.parseColor("#000000"))
                    dataView.textSize = 16f
                    titleView.textSize = 20f
                    titleView.layoutParams = layoutParams
                    titleView.gravity = Gravity.CENTER_HORIZONTAL
                    titleView.setTextColor(Color.parseColor("#FFFFFF"))
                    val elementLessons: Elements = element.eq(i).select("div[class=col-md-12 py-1 lesson]")
                    val title: String = element.select("div[class=card-title]").eq(i).text()
                    stringBuilder.append("<h1>$title</h1><br>\n\n")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        titleView.text = Html.fromHtml("<center><b>$title</b></center>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                    for (k in 0 until elementLessons.size) {
                        val rowElem: Elements = elementLessons.eq(k).select("div[class=row]")
                        val time = elementLessons.eq(k).select("div[class=text-sm-left text-center pr-0 col-12 col-sm-3 col-md-2 col-lg-2]").first().text()
                        for (j in 0 until rowElem.size) {
                            val type = rowElem.eq(j).select("div[class=pr-0 col-2 col-sm-2 col-md-1 col-lg-1 lesson-type]").text()
                            val weekOdd = rowElem.eq(j).select("div[class=col-1 lesson-week nopadding circle  up-circle ]").attr("data-toggle")
                            val weekEven = rowElem.eq(j).select("div[class=col-1 lesson-week nopadding circle  down-circle ]").attr("data-toggle")
                            val weekEvery = rowElem.eq(j).select("div[class=col-1 lesson-week nopadding circle ]").attr("data-toggle")
                            val name = rowElem.eq(j).select("div[class=pr-0 col-9 col-sm-6 col-md-4 col-lg-4 lesson-name]").text()
                            val teacher = rowElem.eq(j).select("div[class=pr-0 col-7 col-sm-5 col-md-3 col-lg-3]").text()
                            val room = rowElem.eq(j).select("div[class=text-right col-5 col-sm col-md col-lg]").text()
                            if(weekEvery == "tooltip") containsEvery = true
                            if(weekEven == "tooltip") containsEven = true
                            if(weekOdd == "tooltip") containsOdd = true
                            when(evenOdd) {
                                1 -> {
                                    if(weekOdd == "tooltip" || weekEvery == "tooltip") {
                                        stringBuilder.append("<div><b>$time</b> $type. $name</div><div>Преподаватель: $teacher</div><div>Кабинет: $room</div><hr>")
                                    }
                                }
                                2 -> {
                                    if(weekEven == "tooltip" || weekEvery == "tooltip") {
                                        stringBuilder.append("<div><b>$time</b> $type. $name</div><div>Преподаватель: $teacher</div><div>Кабинет: $room</div><hr>")
                                    }
                                }
                            }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        dataView.text = Html.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                    titleCardView.addView(titleView)
                    innerCardView.addView(titleCardView)
                    innerCardView.addView(dataView)
                    outerCardView.addView(innerCardView)
                    when (evenOdd) {
                        1 -> {
                            if(containsOdd || containsEvery) {
                                linearLayout.addView(outerCardView)
                                if (!weekChange) {
                                    linearLayout.alpha = 0f
                                    linearLayout.animate().setDuration(300).alpha(1f)
                                }
                            }
                        }
                        2-> {
                            if(containsEven || containsEvery) {
                                linearLayout.addView(outerCardView)
                                if (!weekChange) {
                                    linearLayout.alpha = 0f
                                    linearLayout.animate().setDuration(300).alpha(1f)
                                }
                            }
                        }
                    }

                }

            }
            catch(e: IOException) {
                Toast.makeText(applicationContext, "Error: $e", Toast.LENGTH_SHORT).show()
            }

        }

        fun getTimetable() {
            Thread {
                val stringBuilder = StringBuilder()
                val timetableView: TextView = findViewById(R.id.timetableView)
                try {
                    val doc: Document = Jsoup.connect(intent.getStringExtra("url")).get()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        runOnUiThread {
                            val sharedPref = getSharedPreferences("favourites", 0)
                            val editor = sharedPref.edit()
                            editor.putString("Temp", doc.select(".card-body.p-0").toString())
                            timetableView.visibility = View.GONE
                            showTimetable(doc.select(".card-body.p-0").toString(), false)
                            for (i in 1..5) {
                                if (sharedPref.contains("FavUrl$i") || sharedPref.contains("FavName$i")) {
                                    if (sharedPref.getString("FavUrl$i", "0") == intent.getStringExtra("url")) {
                                        editor.putString("FavData$i", doc.select(".card-body.p-0").toString())
                                        break
                                    }
                                }
                            }
                            editor.apply()
                        }
                    }
                    runOnUiThread {
                        timetableView.alpha = 0f
                        timetableView.animate().setDuration(300).alpha(1f)
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        val sharedPref = getSharedPreferences("favourites", 0)
                        val editor = sharedPref.edit()
                        var favouriteFound = false
                        for (i in 1..5) {
                            if (sharedPref.contains("FavUrl$i") || sharedPref.contains("FavName$i")) {
                                if (sharedPref.getString("FavUrl$i", "0") == intent.getStringExtra("url")) {
                                    timetableView.visibility = View.GONE
                                    editor.putString("Temp", sharedPref.getString("FavUrl$i", "0").toString())
                                    editor.apply()
                                    favouriteFound = true
                                    showTimetable(sharedPref.getString("FavData$i", "0").toString(), false)
                                    break
                                }
                            }
                        }
                        if (!favouriteFound) {
                            val radioButton: RadioGroup = findViewById(R.id.radioGroup)
                            radioButton.visibility = View.GONE
                            timetableView.visibility = View.VISIBLE
                            timetableView.text =
                                stringBuilder.append("\n").append("Error: ").append(e.message)
                                    .append("\n\n")
                        }
                    }
                }
            }.start()
        }

        fun clearFavourites() {
            val sharedPref = getSharedPreferences("favourites", 0)
            val editor = sharedPref.edit()
            for (i in 1..5) {
                editor.remove("FavUrl$i")
                editor.remove("FavName$i")
                editor.remove("FavData$i")
                editor.apply()
            }
        }

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        //clearFavourites()
        getTimetable()
        actionBar!!.title = intent.getStringExtra("group_name")
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)

        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val sharedPref = getSharedPreferences("favourites", 0)
                var favouriteFound = false
                for (i in 1..5) {
                    if (sharedPref.contains("FavUrl$i") || sharedPref.contains("FavName$i")) {
                        if (sharedPref.getString("FavUrl$i", "0") == intent.getStringExtra("url")) {
                            favouriteFound = true
                            showTimetable(sharedPref.getString("FavData$i", "0").toString(), true)
                            break
                        }
                    }
                }
                if(!favouriteFound)
                    showTimetable(sharedPref.getString("Temp", "0").toString(), true)
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.timetable_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val sharedPref = getSharedPreferences("favourites", 0)
        for (i in 1..5) {
            if (sharedPref.contains("FavUrl$i") || sharedPref.contains("FavName$i")) {
                if (sharedPref.getString("FavUrl$i", "0") == intent.getStringExtra("url")) {
                    val favouritesText: MenuItem = menu.findItem(R.id.add_to_favourites)
                    favouritesText.title = getString(R.string.delete_favourite)
                    break
                }
            }
            else {
                val favouritesText: MenuItem = menu.findItem(R.id.add_to_favourites)
                favouritesText.title = getString(R.string.add_favourite)
                break
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                actionBar?.setDisplayHomeAsUpEnabled(false)
                return true
            }

            R.id.add_to_favourites -> {
                val sharedPref = getSharedPreferences("favourites", 0)
                val editor = sharedPref.edit()
                for (i in 1..5) {
                    if (sharedPref.contains("FavUrl$i") || sharedPref.contains("FavName$i")) {
                        if (sharedPref.getString("FavUrl$i", "0") == intent.getStringExtra("url")) {
                            Toast.makeText(this, "${getString(R.string.group)} ${sharedPref.getString("FavName$i", "")} ${getString(R.string.deleted_from_favourites)}", Toast.LENGTH_SHORT).show()
                            editor.remove("FavUrl$i")
                            editor.remove("FavName$i")
                            editor.remove("FavData$i")
                            editor.apply()
                            return true
                        }

                    }
                    else {
                        editor.putString("FavUrl$i", intent.getStringExtra("url"))
                        editor.putString("FavName$i", intent.getStringExtra("group_name"))
                        editor.putString("FavData$i", sharedPref.getString("Temp", "0"))
                        editor.apply()
                        Toast.makeText(this, "${getString(R.string.group)} ${sharedPref.getString("FavName$i", "")} ${getString(R.string.added_to_favourites)}", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
                Toast.makeText(this, getString(R.string.limit_exceed), Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed()
    {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

}