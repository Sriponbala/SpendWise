package com.example.spendwise

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Pattern

object Helper {

    fun formatPercentage(percent: Int): String {
        return if(percent > 100) {
            "+100"
        } else {
            percent.toString()
        }
    }

    fun retrieveValueFromScientificNotation(scientificValue: Float): String {
        val format = DecimalFormat("0", DecimalFormatSymbols(Locale.getDefault()))
        return format.format(scientificValue)
    }

    fun validateAmountField(amountEditText: EditText) {
        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                Log.e("Validate", input +" - input")

                // Check if the input starts with a dot, then add a "0" before the dot
                if (input.startsWith(".")) {
                    amountEditText.setText("0$input")
                    amountEditText.setSelection(2)
                }

                // Remove all non-digit and non-dot characters
                val cleanInput = input.replace("[^\\d.]".toRegex(), "")

                // Check if the input contains more than one dot, then remove all dots after the first dot
                val dotIndex = cleanInput.indexOf(".")
                if (dotIndex >= 0 && cleanInput.indexOf(".", dotIndex + 1) > 0) {
                    amountEditText.setText(cleanInput.replace(".", ""))
                    amountEditText.setSelection(dotIndex + 1)
                }

                // Check if the input contains more than two digits after the dot, then remove all digits after the second digit
                if (dotIndex >= 0 && cleanInput.length - dotIndex > 3) {
                    amountEditText.setText(cleanInput.substring(0, dotIndex + 3))
                    amountEditText.setSelection(dotIndex + 3)
                }

                // Check if the input contains more than 10 digits in total, then remove all digits after the tenth digit
                if (cleanInput.length > 10) {
                    amountEditText.setText(cleanInput.substring(0, 10))
                    amountEditText.setSelection(10)
                }
            }
        })

    }

    fun validateEmail(email: String): String? {
        return if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            null
        } else if(email.isEmpty()) {
            "Email should not be empty!"
        } else {
            "Please enter valid email!" //(Eg:sample@abc.com)|Required format: example@abcd.com
        }
    }

    fun validatePasswordText(password: String): String? {
        return if(password.isEmpty()) {
            "Password should not be empty!"
        } else if(!password.matches(".*[A-Z].*".toRegex())) {
            "Should contain atleast 1 uppercase"
        } else if(!password.matches(".*[a-z].*".toRegex())) {
            "Should contain atleast 1 lowercase"
        } else if(!password.matches(".*[@#\$%^&*].*".toRegex())) {
            "Should contain atleast 1 special character"
        } else if(password.length < 6) {
            "Should contain atleast 6 characters"
        } else if(password.length > 15) {
            "Should contain maximum 15 characters only"
        } else null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        Log.e("SignupFragment", "confirm - ${password == confirmPassword}")
        return if(password == confirmPassword) {
            null
        } else {
            "Password not matching!"
        }
    }

    fun formatNumberToIndianStyle(number: Float): String {
        val format = DecimalFormat("#,##,##0.00", DecimalFormatSymbols(Locale("en", "IN")))
        return format.format(number)
    }

    fun formatNumberToIndianStyleWithoutDecimal(number: Float): String {
        val format = DecimalFormat("#,##,##0", DecimalFormatSymbols(Locale("en", "IN")))
        return format.format(number)
    }
    fun setupEditTextValidation(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                editText.removeTextChangedListener(this)

                val input = editText.text.toString()

                val cleanedInput = input.replace(Regex("[^\\d.]"), "")

                val parts = cleanedInput.split(".")
                val digitsBeforeDecimal = if (parts.isNotEmpty()) parts[0].length else 0
                val digitsAfterDecimal = if (parts.size > 1) parts[1].length else 0

                if (digitsBeforeDecimal > 5 || digitsBeforeDecimal < 1 || digitsAfterDecimal > 2) {
                    editText.error = """Invalid input
                        |Min 1 and Max 5 digits before decimal, Max 2 digits after decimal
                    """.trimMargin()
                } else {
                    editText.error = null
                }
                editText.addTextChangedListener(this)
            }
        })
    }

    fun validateAmount(amount: String): Boolean {
        val result = Pattern.matches("^\\d{1,5}(\\.\\d{0,2})?\$", amount)
        Log.e("Amount", result.toString())
        return result
    }

    fun validateGoalAmount(amount: String): Boolean {
        val result = Pattern.matches("^\\d{1,10}\$", amount)
        Log.e("Amount", result.toString())
        return result
    }


/*    fun <E : Enum<E>> showMenu(title: String, enumArray: Array<E>) {
        var sno = 1
        println("-------------${title.uppercase()}-------------")
        for (element in enumArray) {
            println("${sno++}. $element")
        }
    }

    fun <E : Enum<E>> getUserChoice(enumArray: Array<E>): E {
        val option = readOption("one", enumArray.size)
        return enumArray[option - 1]
    }

    fun confirm(index: Int): Boolean {
        while (true) {
            println("Confirm: ")
            for (option in Confirmation.values()) {
                println("${option.ordinal + 1}. ${option.list[index]}")
            }
            val option = readOption("one", Confirmation.values().size)
            return when (Confirmation.values()[option - 1]) {
                Confirmation.CONTINUE -> true
                Confirmation.GO_BACK -> false
            }
        }
    }

    private fun checkValidRecord(option: Int, size: Int): Boolean {
        return option in 1..size
    }

    fun generateOTP(): String {
        return Random.nextInt(100000, 1000000).toString()
    }

    fun verifyOtp(currentOtp: String, generatedOtp: String): Boolean {
        return currentOtp == generatedOtp
    }

    private fun confirmPassword(confirmPassword: String, password: String): Boolean {
        return confirmPassword == password
    }

    private fun validateMobileNumber(number: String): Boolean { // 10-digit Phone number
        return Pattern.matches("^\\d{10}$", number)
    }

    private fun validateEmail(email: String): Boolean {
        return Pattern.matches("^[a-z0-9_!#$.-]{3,30}+@[a-z]{3,20}+.[a-z]{2,3}+\$", email)
    }

    private fun validatePasswordPattern(password: String): Boolean {
        return Pattern.matches("^[a-zA-Z0-9!#@$%^&*_+`~]{4,8}+$", password)
    }

    private fun validatePincode(pincode: String): Boolean {
        return Pattern.matches("^[1-9][0-9]{2}\\s?[0-9]{3}$", pincode)
    }

    private fun validateAddressFields(fieldValue: String): Boolean {
        return Pattern.matches("^[a-zA-Z1-9][a-zA-Z0-9-.\\s]{0,30}$", fieldValue)
    }

    fun generateOrderedDate(): LocalDate {
        return LocalDate.of(2022, 11, 18).plusDays(Random.nextLong(91))
    }

    private fun readString(field: String, message: String): String {
        println(message)
        var input = readLine()
        while ((input == null) || (input == "") || input.isBlank()) {
            println("${field.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} should not be empty! Re-enter $field:")
            input = readLine()
        }
        return input
    }

    private fun readInt(): Int {
        var input = readLine()
        while (input == null || input.toIntOrNull() == null) {
            println("Option must contain integers only! Re-enter option:")
            input = readLine()
        }
        return input.toIntOrNull()!!
    }

    fun readName() = readString("name", "Enter name:")

    fun readMobileNumber(): String {
        while (true) {
            val mobile = readString(
                "mobile", """Enter mobile number:
                |[Should contain 10 digits] 
            """.trimMargin()
            )
            if (validateMobileNumber(mobile)) {
                return mobile
            }
        }
    }

    fun readEmail(): String {
        while (true) {
            val email = readString(
                "email", """Enter email:
                |[Format: localpart@example.com] 
            """.trimMargin()
            )
            if (validateEmail(email)) {
                return email
            }
        }
    }

    fun readPassword(): String {
        while (true) {
            val password = readString(
                "password", """Enter password:
                |[Password can contain any of the following : a-zA-Z0-9!#@${'$'}%^&*_+`~]
                |[It should contain 4 to 8 characters]""".trimMargin()
            )
            if (validatePasswordPattern(password)) {
                return password
            }
        }
    }

    fun readConfirmPassword(password: String): String {
        while (true) {
            val confirmPassword = readString("confirm password", "Enter confirm password")
            if (confirmPassword(confirmPassword, password)) {
                return confirmPassword
            }
        }
    }

    fun readOTP() = readString("OTP", "Enter OTP:")

    fun readAddressField(field: String): String {
        while (true) {
            val addressField = readString(field, "Enter $field:")
            if (validateAddressFields(addressField)) {
                return addressField
            }
        }
    }

    fun readPincode(): String {
        while (true) {
            val pincode = readString(
                "pin-code", """Enter pin-code: 
                |[Should not be empty,
                |Should start with number > 0,
                |must contain 6 digits,
                |[Format: 600062 OR 600 062]
            """.trimMargin()
            )
            if (validatePincode(pincode)) {
                return pincode
            }
        }
    }

    fun readOption(field: String, size: Int): Int {
        var option: Int
        while (true) {
            println("Select $field:")
            option = readInt()
            if (checkValidRecord(option, size)) {
                return option
            } else {
                println("Invalid option! Try again!")
            }
        }
    }

    fun readProductName() = readString("product name", "Enter product name:")

    fun getQuantity(skuId: String, cartActivities: CartActivitiesContract): Int {
        return getQuantity(skuId, cartActivities = cartActivities, checkOutActivities = null)
    }

    fun getQuantity(skuId: String, checkOutActivities: CheckOutActivitiesContract): Int {
        return getQuantity(skuId, cartActivities = null, checkOutActivities = checkOutActivities)
    }

    private fun getQuantity(
        skuId: String,
        cartActivities: CartActivitiesContract?,
        checkOutActivities: CheckOutActivitiesContract?
    ): Int {
        val quantity: Int
        while (true) {
            println("Enter the quantity required: ")
            val input = readInt()
            if (input in 1..4) {
                val availableQuantity = checkOutActivities?.getAvailableQuantityOfProduct(skuId)
                    ?: cartActivities?.getAvailableQuantityOfProduct(skuId)
                if (availableQuantity != null) {
                    if (availableQuantity >= input) {
                        quantity = input
                        break
                    } else {
                        println("Only $availableQuantity items available!")
                    }
                }
            } else {
                if (input < 1) {
                    println("You should select atleast 1 item!")
                } else {
                    println("You can select a maximum of 4 items!")
                }
            }
        }
        return quantity
    }*/

}
