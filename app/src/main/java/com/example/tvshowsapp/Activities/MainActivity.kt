package com.example.tvshowsapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvshowsapp.Models.HomeViewModel
import com.example.tvshowsapp.Network.ApiClient
import com.example.tvshowsapp.Network.ApiInterface
import com.example.tvshowsapp.R
import com.example.tvshowsapp.databinding.ActivityMainBinding


import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


var currentPage = 1
class MainActivity : AppCompatActivity() {

    lateinit var arrayList: ArrayList<HomeViewModel>
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.imageSearch.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        arrayList = ArrayList()
        retroFitArray()
        Log.d("Log1", "onCreateView invoked")
    }


    private fun retroFitArray() {
        Log.d("Log2", "Making Retrofit call")
        binding.progressBar.visibility = View.VISIBLE // Show progress bar
        val apiInterface = ApiClient.client.create(ApiInterface::class.java)
        //page increment
        currentPage++
        val call: Call<JsonObject> = apiInterface.getData(page = 1)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(
                    "Log3",
                    "Data is coming with response code: ${response.code()} and message: ${response.message()}"
                )

                if (response.isSuccessful) {

                    val jsonResponse = JSONObject(response.body().toString())
                    val tvShowsArray = jsonResponse.getJSONArray("tv_shows")

                    Log.d("Log4", "Successfully retrieved tv shows data")
                    Log.d("Log Response", "Full API Response: $tvShowsArray")

                    for (i in 0 until tvShowsArray.length()) {
                        val model = HomeViewModel()
                        val tvShowObject = tvShowsArray.getJSONObject(i)

                        model.id = tvShowObject.getInt("id")
                        model.name = tvShowObject.getString("name")
                        model.permalink = tvShowObject.getString("permalink")
                        model.start_date = tvShowObject.getString("start_date")
                        model.end_date = tvShowObject.optString("end_date", null)
                        model.country = tvShowObject.getString("country")
                        model.network = tvShowObject.getString("network")
                        model.status = tvShowObject.getString("status")
                        model.image_thumbnail_path = tvShowObject.getString("image_thumbnail_path")

                        arrayList.add(model)
                        //Log.d("Log 5", "Total Response: ${arrayList.size}")


                    }
                    buildRecycler()
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("SearchLog5", "Error: ${t.message}")
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun buildRecycler() {
        val recycler: RecyclerView = findViewById(R.id.tvShowsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = DataAdapter(arrayList)


        // Pagination listener
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Load more if we have reached the last item
                if (totalItemCount <= lastVisibleItem + 1) {
                    retroFitArray()
                }
            }
        })
    }

    private class DataAdapter(var list: ArrayList<HomeViewModel>) :
        RecyclerView.Adapter<DataAdapter.DataViewHolder>() {
        class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvShowImage: ImageView = itemView.findViewById(R.id.imageTVShow)
            val tvShowName: TextView = itemView.findViewById(R.id.textName)
            val tvShowNetwork: TextView = itemView.findViewById(R.id.textNetwork)
            val tvShowStart: TextView = itemView.findViewById(R.id.textStarted)
            val tvShowStatus: TextView = itemView.findViewById(R.id.textStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.data_item, parent, false)
            return DataViewHolder(view)
        }

        override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
            val model = list[position]

            // Load image
            Picasso.get().load(model.image_thumbnail_path).into(holder.tvShowImage)

            // Set TV show details
            holder.tvShowName.text = model.name
            holder.tvShowNetwork.text = model.network
            holder.tvShowStart.text = model.start_date
            holder.tvShowStatus.text = model.status
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
}
