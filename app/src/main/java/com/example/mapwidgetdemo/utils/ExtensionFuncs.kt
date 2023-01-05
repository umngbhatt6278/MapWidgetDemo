package com.example.mapwidgetdemo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Patterns
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

/**
 * Created by Priyanka.
 */
fun Context.isConnectedToInternet(): Boolean {
    var isOnline = false
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork
        if (nw == null) {
            isOnline = false
        } else {
            val actNw = connectivityManager.getNetworkCapabilities(nw)
            isOnline =
                actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                ) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo
        isOnline = nwInfo != null && nwInfo.isConnected
    }
    return isOnline
}

fun String.validateEmail() = !Patterns.EMAIL_ADDRESS.matcher(this).matches()

@BindingAdapter("image")
fun ImageView.setImage(resource: Any) {
    Glide
        .with(this)
        .load(resource)
        .into(this)
}
