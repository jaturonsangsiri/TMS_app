package com.siamatic.tms.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun checkForInternet(context: Context): Boolean {
  // register activity with the connectivity manager service
  val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  // if the android version is equal to M
  // or greater we need to use the
  // NetworkCapabilities to check what type of
  // network has the internet connection
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

    // Returns a Network object corresponding to
    // the currently active default data network.
    val network = connectivityManager.activeNetwork ?: return false

    // Representation of the capabilities of an active network.
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  } else {
    // if the android version is below M
    @Suppress("DEPRECATION") val networkInfo =
      connectivityManager.activeNetworkInfo ?: return false
    @Suppress("DEPRECATION")
    return networkInfo.isConnected
  }
}