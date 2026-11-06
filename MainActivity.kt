package com.narendra.appcloner

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("clone_profiles", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private val apps = mutableListOf<Pair<String, String>>() // label, package

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        loadApps()
        renderApps()
    }

    private fun buildScreen() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 18)
            setBackgroundColor(0xFFF4F5FA.toInt())
        }
        val title = TextView(this).apply {
            text = "Narendra App Cloner"
            textSize = 25f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF18213A.toInt())
        }
        root.addView(title)
        root.addView(TextView(this).apply {
            text = "App profiles • App launcher • Clone manager"
            textSize = 13f
            setTextColor(0xFF5D667A.toInt())
            setPadding(0, 6, 0, 18)
        })
        val note = TextView(this).apply {
            text = "महत्वपूर्ण: यह शुरुआती संस्करण प्रोफाइल और ऐप शॉर्टकट मैनेज करता है। Android सुरक्षा के कारण यह दूसरे ऐप की स्वतंत्र कॉपी या अलग निजी डेटा नहीं बनाता।"
            textSize = 13f
            setTextColor(0xFF704B00.toInt())
            setBackgroundColor(0xFFFFF0C9.toInt())
            setPadding(14, 12, 14, 12)
        }
        root.addView(note)
        root.addView(TextView(this).apply {
            text = "आपके फोन में उपलब्ध ऐप"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF18213A.toInt())
            setPadding(0, 20, 0, 8)
        })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; id = 1001 }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        apps.clear()
        resolved.map {
            val name = it.loadLabel(packageManager).toString()
            val pkg = it.activityInfo.packageName
            name to pkg
        }.distinctBy { it.second }.sortedBy { it.first.lowercase() }.forEach { apps.add(it) }
    }

    private fun renderApps() {
        val list = findViewById<LinearLayout>(1001)
        list.removeAllViews()
        if (apps.isEmpty()) {
            list.addView(TextView(this).apply { text = "कोई लॉन्च करने योग्य ऐप नहीं मिला।"; textSize = 15f })
            return
        }
        apps.forEach { (label, pkg) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
                setBackgroundColor(0xFFFFFFFF.toInt())
            }
            val name = TextView(this).apply {
                text = label
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(0xFF20283B.toInt())
            }
            val packageText = TextView(this).apply {
                text = pkg
                textSize = 11f
                setTextColor(0xFF7B8498.toInt())
            }
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val launch = Button(this).apply {
                text = "खोलें"
                setOnClickListener { launchApp(pkg) }
            }
            val profile = Button(this).apply {
                text = "प्रोफाइल +"
                setOnClickListener { addProfile(label, pkg); renderApps() }
            }
            row.addView(launch, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(profile, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            card.addView(name); card.addView(packageText); card.addView(row)
            val count = profileCount(pkg)
            card.addView(TextView(this).apply {
                text = "सेव किए गए प्रोफाइल: $count"
                textSize = 12f
                setTextColor(0xFF52617A.toInt())
            })
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 12)
            list.addView(card, params)
        }
        val profilesButton = Button(this).apply {
            text = "मेरे प्रोफाइल देखें"
            setOnClickListener { showProfiles() }
        }
        list.addView(profilesButton)
    }

    private fun launchApp(pkg: String) {
        val launch = packageManager.getLaunchIntentForPackage(pkg)
        if (launch == null) {
            Toast.makeText(this, "यह ऐप लॉन्च नहीं हो सका", Toast.LENGTH_SHORT).show()
        } else startActivity(launch)
    }

    private fun addProfile(label: String, pkg: String) {
        val data = JSONArray(prefs.getString("items", "[]"))
        val item = JSONObject().put("label", label).put("package", pkg)
            .put("profile", "Profile ${profileCount(pkg) + 1}")
        data.put(item)
        prefs.edit().putString("items", data.toString()).apply()
        Toast.makeText(this, "प्रोफाइल सेव हुआ (यह अलग ऐप इंस्टेंस नहीं है)", Toast.LENGTH_LONG).show()
    }

    private fun profileCount(pkg: String): Int {
        val data = JSONArray(prefs.getString("items", "[]"))
        var count = 0
        for (i in 0 until data.length()) if (data.optJSONObject(i)?.optString("package") == pkg) count++
        return count
    }

    private fun showProfiles() {
        val data = JSONArray(prefs.getString("items", "[]"))
        if (data.length() == 0) {
            Toast.makeText(this, "अभी कोई प्रोफाइल सेव नहीं है", Toast.LENGTH_SHORT).show()
            return
        }
        val labels = ArrayList<String>()
        for (i in 0 until data.length()) {
            val o = data.getJSONObject(i)
            labels.add("${o.optString("label")} — ${o.optString("profile")}")
        }
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("सेव किए गए प्रोफाइल")
            .setItems(labels.toTypedArray()) { _, which -> showProfileActions(which) }
            .setNegativeButton("बंद", null).create()
        dialog.show()
    }

    private fun showProfileActions(index: Int) {
        val data = JSONArray(prefs.getString("items", "[]"))
        val item = data.optJSONObject(index) ?: return
        android.app.AlertDialog.Builder(this)
            .setTitle(item.optString("label"))
            .setMessage("यह सेव किया गया प्रोफाइल रिकॉर्ड है। Android ऐप का निजी डेटा इससे अलग नहीं होता।")
            .setPositiveButton("मूल ऐप खोलें") { _, _ -> launchApp(item.optString("package")) }
            .setNeutralButton("प्रोफाइल हटाएँ") { _, _ ->
                val updated = JSONArray()
                for (i in 0 until data.length()) if (i != index) updated.put(data.getJSONObject(i))
                prefs.edit().putString("items", updated.toString()).apply()
                renderApps()
            }
            .setNegativeButton("बंद", null).show()
    }
}
