package com.mtoader.near.model

data class NearLogsUser (var username: String,
                         var password: String,
                         var token: String) {

    fun toJSON(): String {
        return ("{'username': '$username',"
                + "'password':'$password'"
                + "]}")
    }
}