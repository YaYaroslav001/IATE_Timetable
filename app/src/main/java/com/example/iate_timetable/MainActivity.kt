package com.example.iate_timetable

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.marginTop
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.navigation.NavigationView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var locale: Locale
    var maxInfoHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_IATE_Timetable)
        setContentView(R.layout.activity_main)

        fun getInfo(infoView: TextView, reconnectButton: Button, searchView: SearchView) {  //Получение информации со страницы сайта.
            Thread {
                val stringBuilder = StringBuilder()
                try {
                    val doc: Document = Jsoup.connect("http://timetable.iate.obninsk.ru/").get()    //Получение страницы в виде документа.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        runOnUiThread {
                            infoView.text = Html.fromHtml(
                                doc.select(".col-lg-12.pt-0.pt-lg-3.main-notice").toString(),
                                Html.FROM_HTML_MODE_COMPACT
                            )   //Вывод информации с сайта.
                            //infoView.height = infoView.lineHeight * infoView.lineCount  //Установление размера InfoView.
                        }
                    }
                    runOnUiThread {
                        searchView.alpha = 0f
                        infoView.alpha = 0f
                        searchView.visibility = View.VISIBLE
                        reconnectButton.visibility = View.GONE
                        infoView.animate().setDuration(300).alpha(1f)
                        searchView.animate().setStartDelay(300).setDuration(300).alpha(1f)
                    }   //Анимация InfoView и SearchView
                } catch (e: IOException) {  //Обработка ошибок.
                    runOnUiThread {
                        infoView.text =
                            stringBuilder.append("\n").
                            append(getString(R.string.error)).append(e.message).append("\n")
                        reconnectButton.visibility = View.VISIBLE
                        searchView.visibility = View.GONE
                    }
                }
            }.start()
        }

        fun createButtonDynamically(buttonLayout: LinearLayout, text: String, url: String, id: Int) {   //Создание кнопок с названиями групп.
            val dynamicButton = Button(this, null, 0, R.style.dynamicButtonStyle)
            dynamicButton.elevation = 20f   //Тень от кнопки.
            dynamicButton.id = id   //Идентификатор кнопки.
            dynamicButton.text = text   //Текст кнопки (название группы).
            dynamicButton.alpha = 0f
            dynamicButton.translationY = -10f
            buttonLayout.addView(dynamicButton) //Добавление кнопки на ButtonLayout.
            dynamicButton.setOnClickListener {  //Обработка нажатия кнопки.
                val i = Intent(this, TimetableActivity::class.java) //Создание новой активности.
                i.putExtra("url", url)  //Передача в новую активность ссылки на группу.
                i.putExtra("group_name", dynamicButton.text)    //Передача в новую активность названия группы.
                startActivity(i)    //Старт активности.
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)  //Анимация при переходе в новую активность.
            }
            dynamicButton.animate().setDuration(300).alpha(1f)  //Анимация кнопки при её создании.
        }

        fun getGroups (buttonLayout: LinearLayout, search: String) {    //Получение списка групп.
            Thread {
                try {
                    val doc: Document = Jsoup.connect("http://timetable.iate.obninsk.ru/").get()    //Получение документа со списком групп.
                    val links = doc.select(".found-group").select("a")
                    runOnUiThread {
                        var i = 1
                        val placeholder = TextView(this, null, 0)
                        placeholder.textSize = 1f
                        buttonLayout.addView(placeholder)
                        for (link in links) {   //Создание нескольких кнопок.
                            if (link.text().contains(search, ignoreCase = true)) {
                                createButtonDynamically(buttonLayout, link.text(),
                                        link.attr("href"), i)
                                ++i
                            }
                        }
                        if (buttonLayout.childCount == 1){  //Если группа не обнаружена, вывод соответствующего сообщения.
                            val dynamicTextview = TextView(this)
                            dynamicTextview.text = getString(R.string.Nothing_found)
                            dynamicTextview.alpha = 0f
                            dynamicTextview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            dynamicTextview.gravity = Gravity.CENTER_HORIZONTAL
                            dynamicTextview.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            dynamicTextview.textSize = 18f
                            buttonLayout.addView(dynamicTextview)
                            dynamicTextview.animate().setDuration(300).alpha(1f)
                        }
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        val errorView = TextView(this)
                        val stringBuilder = StringBuilder()
                        buttonLayout.addView(errorView)
                        errorView.text =
                            stringBuilder.append("\n").
                            append("Error: ").append(e.message).append("\n")
                    }
                }
            }.start()
        }

        fun foldInfo(infoView: TextView, infoHeight: Int) {
            val valueAnimator = ValueAnimator.ofInt(infoHeight, 0)
            valueAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                infoView.height = value
            }
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.duration = 300
            valueAnimator.start()
        }

        fun setLocale(localeName: String) {
            var currentLang: String? = null
            var currentLanguage = intent.getStringExtra(currentLang).toString()
            if (localeName != currentLanguage) {
                locale = Locale(localeName)
                val res = resources
                val dm = res.displayMetrics
                val conf = res.configuration
                conf.locale = locale
                res.updateConfiguration(conf, dm)
                val refresh = Intent(
                    this,
                    MainActivity::class.java
                )
                refresh.putExtra(currentLang, localeName)
                startActivity(refresh)
            }
        }

        val infoView: TextView = findViewById(R.id.infoView)
        val reconnectButton: Button = findViewById(R.id.reconnectButton)
        val buttonLayout: LinearLayout = findViewById(R.id.buttonLayout)
        val searchView: SearchView = findViewById(R.id.searchView)
        val actionBar: ActionBar? = supportActionBar
        var maxInfoHeightMeasured = false
        supportActionBar?.title = getString(R.string.title_activity_main)
        infoView.movementMethod = LinkMovementMethod.getInstance()
        searchView.visibility = View.GONE
        getInfo(infoView, reconnectButton, searchView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setBackgroundColor(ContextCompat.getColor(this, R.color.site2))
        navView.bringToFront()
        navView.requestLayout()
        val sharedPrefLang = getSharedPreferences("language", 0)
        val editor = sharedPrefLang.edit()
        if (!sharedPrefLang.contains("lang")) {
            editor.putString("lang", "en")
            editor.apply()
        }
        if(sharedPrefLang.getString("lang", "") == "en") {
            setLocale("en")
        }
        if(sharedPrefLang.getString("lang", "") == "ru") {
            setLocale("ru")
        }

        reconnectButton.setOnClickListener(object : View.OnClickListener {
            @SuppressLint("SetTextI18n")
            override fun onClick(view: View?) {
                    reconnectButton.visibility = View.GONE
                    infoView.text = "\n${getString(R.string.loading)}"
                    getInfo(infoView, reconnectButton, searchView)
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    buttonLayout.removeAllViews()
                    getGroups(buttonLayout, query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val currentHeight: Int = infoView.measuredHeight
                if (!maxInfoHeightMeasured) {
                    maxInfoHeight = infoView.height
                    maxInfoHeightMeasured = true
                }
                if (currentHeight == maxInfoHeight || currentHeight == 0) {
                    if (newText != "" && currentHeight == maxInfoHeight) {
                        foldInfo(infoView, currentHeight)
                        actionBar?.setDisplayHomeAsUpEnabled(true)
                    }
                }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val navView: NavigationView = findViewById(R.id.nav_view)
                navView.menu.clear()
                val sub = navView.menu.addSubMenu(0, 0, Menu.NONE, getString(R.string.favourites))
                for(i in 1..5) {
                    val sharedPref = getSharedPreferences("favourites", 0)
                    if (sharedPref.contains("FavUrl$i") && sharedPref.contains("FavName$i")) {
                        sub.add(0, i, Menu.NONE, sharedPref.getString("FavName$i", "FavUrl$i")).setIcon(
                            ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24)).setOnMenuItemClickListener {
                            val sharedPref = getSharedPreferences("favourites", 0)
                            val i = Intent(this, TimetableActivity::class.java)
                            i.putExtra("url", sharedPref.getString("FavUrl${it.itemId}", "0"))
                            i.putExtra("group_name", sharedPref.getString("FavName${it.itemId}", "0"))
                            startActivity(i)
                            val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            drawerLayout.closeDrawer(Gravity.LEFT)
                            false
                        }
                    }
                }
                navView.menu.add(Menu.NONE, 10, Menu.NONE, getString(R.string.settings)).setIcon(
                    ContextCompat.getDrawable(this, R.drawable.ic_baseline_settings_24)).setOnMenuItemClickListener {
                    val i = Intent(this, SettingsActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    false
                }
                val infoView: TextView = findViewById(R.id.infoView)
                val searchView:SearchView = findViewById(R.id.searchView)
                searchView.clearFocus()
                if (infoView.height == 0) {
                    unfoldInfo(infoView, maxInfoHeight)
//                    infoView.height = infoView.lineHeight * infoView.lineCount
//                    val valueAnimator =
//                        ValueAnimator.ofInt(0, infoView.lineHeight * infoView.lineCount)
//                    valueAnimator.addUpdateListener {
//                        val value = it.animatedValue as Int
//                        infoView.height = value
//                    }
//                    valueAnimator.interpolator = LinearInterpolator()
//                    valueAnimator.duration = 300
//                    valueAnimator.start()
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    return true
                }
                else
                    if(toggle.onOptionsItemSelected(item)){
                        return true
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun unfoldInfo(infoView: TextView, infoHeight: Int) {
        val valueAnimator = ValueAnimator.ofInt(0, infoHeight)
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            infoView.height = value
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 300
        valueAnimator.start()
    }

    override fun onBackPressed()
    {
        val infoView: TextView = findViewById(R.id.infoView)
        val searchView:SearchView = findViewById(R.id.searchView)
        searchView.clearFocus()
        if (infoView.height == 0) {
            unfoldInfo(infoView, maxInfoHeight)
        }
//            infoView.height = infoView.lineHeight * infoView.lineCount
//            val valueAnimator =
//                ValueAnimator.ofInt(0, infoView.lineHeight * infoView.lineCount)
//            valueAnimator.addUpdateListener {
//                val value = it.animatedValue as Int
//                infoView.height = value
//            }
//            valueAnimator.interpolator = LinearInterpolator()
//            valueAnimator.duration = 300
//            valueAnimator.start()
//        }
        else {
            moveTaskToBack(true)
            exitProcess(-1)
        }
    }
}

