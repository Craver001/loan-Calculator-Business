package dev.gameplay.loancal.Class

data class PaymentInfo(
    val borrowerName: String,
    val paymentAmount: Double,
    val date: String
) {
    // Add a no-argument constructor
    constructor() : this("", 0.0, "")
}
