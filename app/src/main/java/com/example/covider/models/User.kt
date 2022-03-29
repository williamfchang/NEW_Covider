package com.example.covider.models

open class User(var name:String, var email: String, var uscID: String, var role: CoviderEnums.UserType)
{

    var courses = mutableListOf<String>()
    var favoriteBuildings = mutableListOf<String>()

    constructor(): this("", "", "", CoviderEnums.UserType.NONE){

    }

    public fun reportUserHealthStat(){

    }

    public fun getListOfUserHealthStats(){

    }

    public fun setSchedule(){

    }

}