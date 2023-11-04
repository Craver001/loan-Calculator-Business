package dev.gameplay.loancal.Class

data class LoanInfo(
    val borrowerName: String = "",
    val loanAmount: Int = 0,
    val loanTerm: String = "",
    val interestRatePerMonth: String = "",
    val monthlyPayment: Int = 0,
    val totalPrincipalWithInterest: Int = 0
)
