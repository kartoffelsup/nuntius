package service.user

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.kartoffelsup.nuntius.api.user.request.CreateUserRequest
import io.github.kartoffelsup.nuntius.api.user.request.LoginRequest
import io.github.kartoffelsup.nuntius.api.user.result.CreateUserResult
import io.github.kartoffelsup.nuntius.api.user.result.FailedLogin
import io.github.kartoffelsup.nuntius.api.user.result.LoginResult
import io.github.kartoffelsup.nuntius.api.user.result.SuccessfulLogin
import io.github.kartoffelsup.nuntius.api.user.result.UserContacts
import io.github.kartoffelsup.nuntius.client.ApiResult
import io.github.kartoffelsup.nuntius.client.Failure
import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import io.github.kartoffelsup.nuntius.client.Success

class UserService(private val nuntiusApiService: NuntiusApiService) {
    suspend fun login(email: String, password: String): LoginResult {
        val request = LoginRequest(email, password)
        val apiResult = nuntiusApiService.post(
            "user/login",
            request,
            LoginRequest.serializer(),
            SuccessfulLogin.serializer()
        )

        return when (apiResult) {
            is Success<*> -> apiResult.payload as SuccessfulLogin
            is Failure -> FailedLogin(apiResult.reason)
        }
    }

    suspend fun signup(username: String, email: String, password: String): Either<String, CreateUserResult> {
        val request = CreateUserRequest(username, email, password)
        val apiResult = nuntiusApiService.post(
            "user",
            request,
            CreateUserRequest.serializer(),
            CreateUserResult.serializer()
        )

        return when (apiResult) {
            is Success<*> -> {
                (apiResult.payload as CreateUserResult).right()
            }
            is Failure -> apiResult.reason.left()
        }
    }


    suspend fun getContacts(credentials: String): ApiResult {
        return nuntiusApiService.get(
            "user/contacts",
            UserContacts.serializer(),
            credentials
        )
    }
}
