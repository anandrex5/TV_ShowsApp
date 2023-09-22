package com.example.tvshowsapp.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvshowsapp.Models.SearchHomeViewModel
import com.example.tvshowsapp.Network.ApiClient
import com.example.tvshowsapp.Network.ApiInterface
import com.example.tvshowsapp.R
import com.example.tvshowsapp.databinding.ActivitySearchBinding
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    lateinit var arrayList: ArrayList<SearchHomeViewModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_search)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        val searchEditText: EditText = findViewById(R.id.searchEditText)
//        val searchImageView: ImageView = findViewById(R.id.imageSearch)


        //Add Listener to the Topbar Back Button
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }


        // Setup the EditorActionListener for EditText
        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                initiateSearch()
                true
            } else {
                false
            }
        }

        // Initiate search when the user clicks the search icon
        binding.imageSearch.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchForShows(query)
            }
        }

        arrayList = ArrayList()
        Log.d("SearchLog1", "onCreateView invoked")
    }



    private fun initiateSearch() {
        //Retrieving text from the EditText
        val query = binding.searchEditText.text.toString()
        if (query.isNotEmpty()) {
            // Calls the function to execute a search with the given query
            searchForShows(query)
            hideKeyboard() // Calls the function to hide the keyboard
        }
    }

    private fun hideKeyboard() {
        //get the current focusedView
        val view = currentFocus
        view?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //request keyboard to hide
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }


    private fun searchForShows(query: String) {
        Log.d("SearchLog2", "Making Retrofit call")
        binding.progressBar.visibility = View.VISIBLE // Show progress bar
        val apiInterface = ApiClient.client.create(ApiInterface::class.java)
        val call: Call<JsonObject> = apiInterface.searchShows(query, 1)

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(
                    "SearchLog3",
                    "Data is coming with response code: ${response.code()} and message: ${response.message()}"
                )

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body().toString())
                    val tvSearchArray = jsonResponse.getJSONArray("tv_shows")
                    Log.d("SearchLog4", "Successfully retrieved tv shows data")
                    Log.d("SearchLog4A", "Full API Response: $tvSearchArray")

                    //clear the array list
                    arrayList.clear()

                    for (i in 0 until tvSearchArray.length()) {
                        val model = SearchHomeViewModel()
                        val tvShowObject = tvSearchArray.getJSONObject(i)

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
                    }

                    //notify adapter when data is changed
                    (binding.searchResultsRecyclerView.adapter as? DataAdapter)?.notifyDataSetChanged()
                    buildRecycler()

                    // Handle visibility based on search results
                    if (arrayList.isEmpty()) {
                        binding.searchResultsRecyclerView.visibility = View.GONE
                        binding.imageNoDataFound.visibility = View.VISIBLE
                        Toast.makeText(this@SearchActivity, "No data found!", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.searchResultsRecyclerView.visibility = View.VISIBLE
                        binding.imageNoDataFound.visibility = View.GONE
                    }
                }
                binding.progressBar.visibility = View.GONE
               }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("MainActivity", "Error: ${t.message}")
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun buildRecycler() {
      //  val recycler: RecyclerView = findViewById(R.id.searchResultsRecyclerView)
        val recycler = binding.searchResultsRecyclerView
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = DataAdapter(arrayList)

    }

    private class DataAdapter(var list: ArrayList<SearchHomeViewModel>) :
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
