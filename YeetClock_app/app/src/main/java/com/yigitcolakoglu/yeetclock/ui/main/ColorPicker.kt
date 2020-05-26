package com.yigitcolakoglu.yeetclock.ui.main

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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


class ColorPicker : Fragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener{
    // TODO: Rename and change types of parameters

    private var listener: OnFragmentInteractionListener? = null
    public var green: Int = 0
    public var red: Int = 0
    public var blue: Int = 0
    public var on: Boolean = true
    val cache = DiskBasedCache(File("/"), 1024 * 1024) // 1MB cap

    // Set up the network to use HttpURLConnection as the HTTP client.
    val network = BasicNetwork(HurlStack())

    // Instantiate the RequestQueue with the cache and network. Start the queue.
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
        val layout = inflater.inflate(R.layout.fragment_color_picker, container, false)
        val colorPanel = layout.findViewById<TextView>(R.id.colorShow)
        layout.findViewById<SeekBar>(R.id.green_seekbar).setOnSeekBarChangeListener(this)
        layout.findViewById<SeekBar>(R.id.blue_seekbar).setOnSeekBarChangeListener(this)
        layout.findViewById<SeekBar>(R.id.red_seekbar).setOnSeekBarChangeListener(this)
        layout.findViewById<Button>(R.id.power_toggle).setOnClickListener(this)
        val url = "http://yeetclock.xyz/getcolor"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                var gson = Gson()
                var led = gson.fromJson(response,Led::class.java)
                red = led.RGB.get(0)
                green = led.RGB.get(1)
                blue = led.RGB.get(2)
                on = led.ON == 1
                val r: SeekBar? = this.view?.findViewById<SeekBar>(R.id.red_seekbar)
                val g: SeekBar? = this.view?.findViewById<SeekBar>(R.id.green_seekbar)
                val b: SeekBar? = this.view?.findViewById<SeekBar>(R.id.blue_seekbar)
                val button: Button? = this.view?.findViewById(R.id.power_toggle)
                val palette: TextView? = this.view?.findViewById(R.id.colorShow)
                palette!!.setBackgroundColor(Color.rgb(red,green,blue))
                Log.i("RED",r.toString())
                Log.i("GREEN",g.toString())
                Log.i("BLUE",b.toString())
                r?.setProgress(red)
                g?.setProgress(green)
                b?.setProgress(blue)
                if (!on) {
                    r?.setVisibility(View.INVISIBLE)
                    g?.setVisibility(View.INVISIBLE)
                    b?.setVisibility(View.INVISIBLE)
                    palette?.setVisibility(View.INVISIBLE)
                    button?.setBackgroundColor(Color.BLACK)
                    button?.setTextColor(Color.WHITE)
                }else{
                    r?.setVisibility(View.VISIBLE)
                    g?.setVisibility(View.VISIBLE)
                    b?.setVisibility(View.VISIBLE)
                    palette?.setVisibility(View.VISIBLE)
                    button?.setBackgroundColor(Color.WHITE)
                    button?.setTextColor(Color.BLACK)
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(getActivity(),"Issue with GET request",Toast.LENGTH_SHORT).show()
            })
        requestQueue.add(stringRequest)
        return layout
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ColorPicker().apply {
                arguments = Bundle().apply {

                }
            }
    }
    public override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (!fromUser){
            return
        }
        val g: Int = this.view?.findViewById<SeekBar>(R.id.green_seekbar)!!.progress
        val r: Int = this.view?.findViewById<SeekBar>(R.id.red_seekbar)!!.progress
        val b: Int = this.view?.findViewById<SeekBar>(R.id.blue_seekbar)!!.progress
        red = r
        blue = b
        green = g
        this.view?.findViewById<TextView>(R.id.colorShow)!!.setBackgroundColor(Color.rgb(r,g,b))
        val url = "http://yeetclock.xyz/setcolor?R=%d&G=%d&B=%d&O=%d"
        Log.i("COLORS",url.format(r,b,g, if (on) 1 else 0))
        val stringRequest = StringRequest(
            Request.Method.GET, url.format(r,g,b,if (on) 1 else 0),
            Response.Listener<String> { response ->
            },
            Response.ErrorListener { error ->
                Toast.makeText(getActivity(),"Issue with GET request",Toast.LENGTH_SHORT).show()
            })
        requestQueue.add(stringRequest)

    }

    public override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    public override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    public override fun onClick(v: View?) {
        on = !on
        val r: SeekBar? = this.view?.findViewById<SeekBar>(R.id.red_seekbar)
        val g: SeekBar? = this.view?.findViewById<SeekBar>(R.id.green_seekbar)
        val b: SeekBar? = this.view?.findViewById<SeekBar>(R.id.blue_seekbar)
        val button: Button? = this.view?.findViewById(R.id.power_toggle)
        val palette: TextView? = this.view?.findViewById(R.id.colorShow)
        if (!on) {
            r?.setVisibility(View.INVISIBLE)
            g?.setVisibility(View.INVISIBLE)
            b?.setVisibility(View.INVISIBLE)
            palette?.setVisibility(View.INVISIBLE)
            button?.setBackgroundColor(Color.BLACK)
            button?.setTextColor(Color.WHITE)
            val url = "http://yeetclock.xyz/setcolor?R=%d&G=%d&B=%d&O=0"
            val stringRequest = StringRequest(
                Request.Method.GET, url.format(red,green,blue),
                Response.Listener<String> { response ->
                },
                Response.ErrorListener { error ->
                    Log.i("ERROR",error.message)
                    Toast.makeText(getActivity(),"Issue with GET request",Toast.LENGTH_SHORT).show()
                })
            requestQueue.add(stringRequest)
        }else{
            r?.setVisibility(View.VISIBLE)
            g?.setVisibility(View.VISIBLE)
            b?.setVisibility(View.VISIBLE)
            palette?.setVisibility(View.VISIBLE)
            button?.setBackgroundColor(Color.WHITE)
            button?.setTextColor(Color.BLACK)
            val url = "http://yeetclock.xyz/setcolor?R=%d&G=%d&B=%d&O=1"
            val stringRequest = StringRequest(
                Request.Method.GET, url.format(red,green,blue),
                Response.Listener<String> { response ->
                },
                Response.ErrorListener { error ->
                    Log.i("ERROR",error.message)
                    Toast.makeText(getActivity(),"Issue with GET request",Toast.LENGTH_SHORT).show()
                })
            requestQueue.add(stringRequest)
        }
    }
}

public class Led(val RGB:List<Int>, val ON:Int)

