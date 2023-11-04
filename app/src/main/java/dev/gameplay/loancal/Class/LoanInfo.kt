package dev.gameplay.loancal.Class

import java.util.Date

data class LoanInfo(
    val borrowerName: String = "",
    val loanAmount: Int = 0,
    val loanTerm: String = "",
    val interestRatePerMonth: String = "",
    val monthlyPayment: Int = 0,
    val totalPrincipalWithInterest: Int = 0,
    val totalLoanPayment:Int = 0,
    val date: Date = Date(),
    val startDate: Date = Date(), // Use valid Date objects here
    val endDate: Date = Date() // Use valid Date objects here
)

