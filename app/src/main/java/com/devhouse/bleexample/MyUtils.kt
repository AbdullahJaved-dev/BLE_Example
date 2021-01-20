package com.devhouse.bleexample

import android.content.pm.PackageManager

/**
 * Created by Abdullah on 1/19/2021.
 */

fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)