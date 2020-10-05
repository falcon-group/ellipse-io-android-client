package com.io.ellipse.data.persistence.preferences.proto

import androidx.datastore.DataStore

open class BasePreferences<T>(
    protected val preferences: DataStore<T>
) : DataStore<T> by preferences