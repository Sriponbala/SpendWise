package com.example.spendwise

import android.content.res.Resources
import android.util.Patterns
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Helper {

    private lateinit var resources: Resources //= Resources.getSystem()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    fun setResources(resources: Resources) {
        this.resources = resources
    }
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun validateName(name: String): Boolean {
        return name.trim().isEmpty()
    }

    fun checkAmountIsZeroOrNot(amount: String): Boolean {
        val result: Boolean = try {
            BigDecimal(amount) == BigDecimal(resources.getString(R.string.zero)) || BigDecimal(amount) == BigDecimal(resources.getString(R.string.zero_one_decimal)) || BigDecimal(amount) == BigDecimal(0)
        } catch (exception: Exception) {
            false
        }
        return result
    }

    fun getDate(date: String): Date {
        return dateFormat.parse(date) ?: Date()
    }

    fun formatPercentage(percent: Int): String {
        return if(percent > 100) {
            resources.getString(R.string.exceededPercent)
        } else {
            percent.toString()
        }
    }

    fun validateEmail(email: String): String? {
        return if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            null
        } else if(email.isEmpty()) {
            resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.email_label))
        } else {
            resources.getString(R.string.enter_valid_email_message)
        }
    }

    fun validatePasswordText(password: String): String? {
        return if(password.isEmpty()) {
            resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.password_label))
        } else if(!password.matches(".*[A-Z].*".toRegex())) {
            resources.getString(R.string.upperCase)
        } else if(!password.matches(".*[a-z].*".toRegex())) {
            resources.getString(R.string.lowerCase)
        } else if(!password.matches(".*[@#\$%^&*].*".toRegex())) {
            resources.getString(R.string.specialCharacter)
        } else if(password.length < 6) {
            resources.getString(R.string.passwordCharactersCountMin)
        } else if(password.length > 15) {
            resources.getString(R.string.passwordCharactersCountMax)
        } else null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return if(password == confirmPassword) {
            null
        } else {
            resources.getString(R.string.confirmPasswordNotCorrect)
        }
    }

    fun formatNumberToIndianStyle(number: Float): String {
        val format = DecimalFormat("#,##,##0.00", DecimalFormatSymbols(Locale("en", "IN")))
        return format.format(number)
    }

    fun formatNumberToIndianStyle(number: BigDecimal): String {
        val format = DecimalFormat("#,##,##0.00", DecimalFormatSymbols(Locale("en", "IN")))
        return format.format(number)
    }

    fun validateAmount(amount: String): Boolean {
        return Pattern.matches("^\\d{1,10}(\\.\\d{0,2})?\$", amount)
    }

    fun formatDecimal(number: BigDecimal): String {
        val decimalFormat = DecimalFormat(resources.getString(R.string.zero))
        return decimalFormat.format(number)
    }

    fun formatDecimalToThreePlaces(number: BigDecimal): String {
        val decimalFormat = DecimalFormat(resources.getString(R.string.zero_three_decimal))
        return decimalFormat.format(number)
    }

    fun validateGoalAmount(amount: String): Boolean {
        return Pattern.matches("^\\d{1,10}(\\.\\d{0,2})?\$", amount)
    }

}
