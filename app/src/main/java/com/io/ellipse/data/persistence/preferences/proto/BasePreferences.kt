package com.io.ellipse.data.persistence.preferences.proto

import androidx.datastore.DataStore
import androidx.datastore.DataStoreFactory
import androidx.datastore.preferences.Preferences

open class BasePreferences<T>(
    protected val preferences: DataStore<T>
) : DataStore<T> by preferences