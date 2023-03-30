package com.example.spendwise

import android.util.Log
import android.util.Patterns
import java.util.*
import java.util.regex.Pattern

object Helper {

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
