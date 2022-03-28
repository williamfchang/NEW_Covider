package com.example.covider.models

class Student(name:String, email: String, studentID: String) : User(name, email, studentID, CoviderEnums.UserType.STUDENT){

}