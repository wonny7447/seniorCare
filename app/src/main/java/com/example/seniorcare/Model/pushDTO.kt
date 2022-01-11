package com.example.seniorcare.Model

data class pushDTO (var to: String,
               var notification: Notification) {
    data class Notification(
        var body : String,
        var title : String
    )
}
