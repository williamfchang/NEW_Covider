package com.example.covider.models

open class User(var name:String, var email: String, var uscID: String, var role: CoviderEnums.UserType)
{

    val courses = mutableListOf<CourseID>()

    public fun reportUserHealthStat(){

    }

    public fun getListOfUserHealthStats(){

    }

    public fun setSchedule(){

    }

}