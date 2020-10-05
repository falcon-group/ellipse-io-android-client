package com.io.ellipse.data.persistence.database.dao.base

interface BaseDao<T> : CreateDao<T>, UpdateDao<T>, DeleteDao<T>