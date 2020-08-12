package com.yigitcolakoglu.yeetclock.ui.main

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

import com.yigitcolakoglu.yeetclock.R
import java.io.File
import java.lang.Math.floor
import java.lang.Math.toIntExact
import java.sql.Time
import java.time.DayOfWeek
import kotlin.math.pow

class AlarmFragment : Fragment() , View.OnClickListener{
    private var listener: OnFragmentInteractionListener? = null
    val cache = DiskBasedCache(File("/"), 1024 * 1024) // 1MB cap
    var host = ""
    // Set up the network to use HttpURLConnection as the HTTP client.
    val network = BasicNetwork(HurlStack())
    val requestQueue = RequestQueue(cache, network).apply {
        start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_alarm, container, false)
        val daySelector = layout.findViewById<MaterialDayPicker>(R.id.alarm_day_picker)
        val timeSelector = layout.findViewById<TimePicker>(R.id.alarm_time_picker)
        val updateButton = layout.findViewById<Button>(R.id.alarm_update_button)
        updateButton.setOnClickListener(this)
        timeSelector.setIs24HourView(true)
        val file = File(activity?.applicationContext?.filesDir, "ip")
        host = file.readText()
        val url = "http://%s/getalarm".format(host)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val gson = Gson()
                val alarm = gson.fromJson(response, Alarm::class.java)
                var enabled_days = arrayListOf<MaterialDayPicker.Weekday>()
                for((k, i) in alarm.days.iterator().withIndex()){
                    if(i == 1) {
                        when (k) {
                            0 -> enabled_days.add(MaterialDayPicker.Weekday.MONDAY)
                            1 -> enabled_days.add(MaterialDayPicker.Weekday.TUESDAY)
                            2 -> enabled_days.add(MaterialDayPicker.Weekday.WEDNESDAY)
                            3 -> enabled_days.add(MaterialDayPicker.Weekday.THURSDAY)
                            4 -> enabled_days.add(MaterialDayPicker.Weekday.FRIDAY)
                            5 -> enabled_days.add(MaterialDayPicker.Weekday.SATURDAY)
                            6 -> enabled_days.add(MaterialDayPicker.Weekday.SUNDAY)
                        }
                    }
                }
                daySelector.setSelectedDays(enabled_days.toList())
                val hour = alarm.time/3600
                val minute = (alarm.time%3600)/60
                timeSelector.hour = hour
                timeSelector.minute = minute
            },
            Response.ErrorListener { error ->
                Toast.makeText(getActivity(),"Issue with GET request", Toast.LENGTH_SHORT).show()
            })
        requestQueue.add(stringRequest)
        return layout
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AlarmFragment().apply {}
    }

    public override fun onClick(v: View?) {
        val enabled_days = this.view?.findViewById<MaterialDayPicker>(R.id.alarm_day_picker)?.selectedDays
        var new_days = arrayListOf<Int>(0,0,0,0,0,0,0)
        if (enabled_days != null) {
            for(i in enabled_days){
                when(i){
                    MaterialDayPicker.Weekday.MONDAY -> new_days[0] = 1
                    MaterialDayPicker.Weekday.TUESDAY -> new_days[1] = 1
                    MaterialDayPicker.Weekday.WEDNESDAY -> new_days[2] = 1
                    MaterialDayPicker.Weekday.THURSDAY -> new_days[3] = 1
                    MaterialDayPicker.Weekday.FRIDAY -> new_days[4] = 1
                    MaterialDayPicker.Weekday.SATURDAY -> new_days[5] = 1
                    MaterialDayPicker.Weekday.SUNDAY -> new_days[6] = 1
                }
            }
        }
        var days_int = 0
        for(i in 0..6){
            days_int += (2.0.pow(i)*new_days[i]).toInt()
        }
        val hour = this.view?.findViewById<TimePicker>(R.id.alarm_time_picker)?.hour?.times(3600)
        val minute = this.view?.findViewById<TimePicker>(R.id.alarm_time_picker)?.minute?.times(60)
        val url = "http://%s/".format(host) + "setalarm?time=%d&days=%d"
        val stringRequest = StringRequest(
            Request.Method.GET, url.format(hour?.let { minute?.plus(it) },days_int),
            Response.Listener<String> { response ->
            },
            Response.ErrorListener { error ->
                Toast.makeText(getActivity(),"Issue with GET request", Toast.LENGTH_SHORT).show()
            })
        requestQueue.add(stringRequest)
    }
}

public class Alarm(val days:List<Int>, val time:Int)

