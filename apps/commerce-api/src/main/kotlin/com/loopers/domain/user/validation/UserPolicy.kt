package com.loopers.domain.user.validation

object UserPolicy {
    object UserName {
        const val message = "영문 및 숫자 10자 이내"
        const val pattern = "^[a-zA-Z0-9]{1,10}$"
    }

    object BirthDate {
        const val message = "yyyy-MM-dd"
        const val pattern = "^\\d{4}-\\d{2}-\\d{2}$"
    }

    object Email {
        const val message = "xx@yy.zz"
        const val pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
    }
}
