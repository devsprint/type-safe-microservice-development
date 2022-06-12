package com.typesafedev.tsmd

object TS002__FunctionalProgramming {


  def getMonthNameByIndex(index: Int): String = {
    val months = Array("January", "February", "March", "April", "May", "June", "July",
      "August", "September", "October", "November", "December")

    months(index -1)
  }

  def main(args: Array[String]): Unit = {
    println(getMonthNameByIndex(1))
    println(getMonthNameByIndex2(100))

  }

  def getMonthNameByIndex2(index: Int): Option[String] = {
    val months = Array("January", "February", "March", "April", "May", "June", "July",
      "August", "September", "October", "November", "December")

    if (index >=0 && index < months.length)
       Some(months(index -1))
    else
      None
  }

}
