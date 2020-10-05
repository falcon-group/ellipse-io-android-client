package com.io.ellipse.domain.repository

sealed class Specification

abstract class UpdateSpec : Specification()

abstract class DeleteSpec : Specification()

abstract class RetrieveSpec : Specification()