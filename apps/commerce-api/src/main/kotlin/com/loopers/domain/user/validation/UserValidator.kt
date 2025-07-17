package com.loopers.domain.user.validation

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType

object UserValidator {

    fun validateUserName(userName: String) {
        if (!Regex(UserPolicy.UserName.pattern).matches(userName)) {
            throw CoreException(ErrorType.BAD_REQUEST, UserPolicy.UserName.message)
        }
    }

    fun validateBirthDate(birthDate: String) {
        if (!Regex(UserPolicy.BirthDate.pattern).matches(birthDate)) {
            throw CoreException(ErrorType.BAD_REQUEST, UserPolicy.BirthDate.message)
        }
    }

    fun validateEmail(email: String) {
        if (!Regex(UserPolicy.Email.pattern).matches(email)) {
            throw CoreException(ErrorType.BAD_REQUEST, UserPolicy.Email.message)
        }
    }
}
