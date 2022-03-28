package com.example.covider.models

class Instructor(name:String, email: String, instructorID: String) : User(name, email, instructorID, CoviderEnums.UserType.INSTRUCTOR){

}