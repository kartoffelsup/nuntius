package service.user

import io.github.kartoffelsup.nuntius.api.user.request.LoginRequest
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

    suspend fun getContacts(credentials: String): ApiResult {
        return nuntiusApiService.get(
            "user/contacts",
            UserContacts.serializer(),
            credentials
        )
    }
}
