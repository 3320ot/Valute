package com.android.mytest

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.net.URL


var text = ""
var name = arrayOf("error", "error")
var n = ""

class Course : AppCompatActivity() {
    private var title : TextView? = null
    private var all : LinearLayout? = null
    private var size = 0
    private var save : SharedPreferences? = null

    override fun onStart() {
        super.onStart()
        save = getSharedPreferences("DATA", Context.MODE_PRIVATE)
        text = save?.getString("json",text)!!
        update()
        Thread{
            while(true){
                update()
                Thread.sleep(15_000)
            }
        }.start()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course)

        title = findViewById(R.id.name)
        all = findViewById(R.id.all)
        findViewById<FloatingActionButton>(R.id.update).setOnClickListener {
            update()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun update(){
        Thread {
            try {
                text = URL("https://www.cbr-xml-daily.ru/daily_json.js").readText()
            } catch (e: Exception) {
                Log.println(Log.ERROR, "Network error", e.stackTraceToString())
//                runOnUiThread { Toast.makeText(this, "Проверьте правильность ссылки или доступность интернет соединения", Toast.LENGTH_SHORT).show() }
            }
            if (text != ""){
                save?.edit()?.putString("json", text)?.apply()
                name[0] = Regex("""\d\d\d\d-\d\d-\d\d""").find(text)?.groupValues?.get(0) ?: String()
                name[1] = Regex("""\d\d:\d\d""").find(text)?.groupValues?.get(0) ?: String()
                n = "Курс валют на: ${name[0]} ${name[1]}"
                size = Regex("ID").findAll(text).count()
                val code_list : Sequence<MatchResult> = Regex("CharCode\": \".+\"").findAll(text)
                val name_list : Sequence<MatchResult> = Regex("Name\": \".+\"").findAll(text)
                val value_list : Sequence<MatchResult> = Regex("Value\": .+,").findAll(text)

                runOnUiThread {

                    title?.text = n
                    all?.removeAllViews()
                    for (i in 1..size){
                        val position = LinearLayout(this)
                        position.gravity = Gravity.CENTER_HORIZONTAL
                        position.orientation=LinearLayout.HORIZONTAL
                        position.dividerPadding=10
                        val code = TextView(this)
                        code.text= code_list.elementAt(i-1).value.slice(IntRange(12, 14))
                        val names = TextView(this)
                        names.setPadding(20, 15, 20, 15)
                        names.text= name_list.elementAt(i-1).value.slice(IntRange(8, name_list.elementAt(i-1).value.length-2))
                        val values = TextView(this)
                        values.text= value_list.elementAt(i-1).value.slice(IntRange(8, value_list.elementAt(i-1).value.length-2))

                        names.width = 400
                        position.addView(code)
                        position.addView(names)
                        position.addView(values)
                        position.setOnClickListener {
                            val converter = Dialog(this)
                            val conv = EditText(this)
                            val pole = LinearLayout(this)
                            val a = TextView(this)
                            val b = TextView(this)
                            val but = Button(this)
                            a.apply {
                                setPadding(12)
                                textSize=18F
                                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                                text=names.text
                            }
                            b.text = "Сумма в ${code.text}"
                            but.apply{
                                but.text = "Конвертировать"
                                setPadding(12)
                                but.setOnClickListener{
                                    b.text = (conv.text.toString().toFloat()*values.text.toString().toFloat()).toString()
                                }
                            }
                            conv.hint = "Введите сумму в рублях"
                            conv.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            pole.apply {
                                setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
                                addView(a)
                                addView(conv)
                                addView(b)
                                addView(but)
                                orientation=LinearLayout.VERTICAL
                            }
                            converter.addContentView(pole, ViewGroup.LayoutParams(620, ViewGroup.LayoutParams.WRAP_CONTENT))
                            converter.show()
                        }

                        all?.addView(position)
                    }
                }
            }
        }.start()
    }
}
