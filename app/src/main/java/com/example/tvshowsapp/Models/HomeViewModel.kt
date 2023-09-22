package com.example.tvshowsapp.Models

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    var id: Int? = null
    var name: String? = null
    var permalink: String? = null
    var start_date: String? = null
    var end_date: String? = null
    var country: String? = null
    var network: String? = null
    var status: String? = null
    var image_thumbnail_path: String? = null

}