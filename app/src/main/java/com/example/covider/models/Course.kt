package com.example.covider.models

typealias CourseID = String

class Course(val title: String,
             val days: List<String>,
             startTime: Int = 0,
             endTime: Int = 0,
             buildingID: String) : Visit(startTime, endTime, buildingID) {

    val instructors = mutableListOf<Instructor>()
    val students = mutableListOf<Student>()
    var mode: CoviderEnums.ClassMode = CoviderEnums.ClassMode.INPERSON
}