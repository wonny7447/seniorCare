package com.example.seniorcare.Model

class LoginUserData (val uid : String,
                     val relation_uid : String,
                     val relation : String,
                     val userName : String,
                     val userEmail : String,
                     val userPhotoUrl : String,
                     val loginTime : Long,
                     val birth : String,
                     val sex : String,
                     val height : String,
                     val weight : String,
                     val protect : String) {

    constructor() : this("","", "", "","","",0,"", "", "", "", "")
}