package login

import form.formField
import io.github.kartoffelsup.nuntius.api.user.request.LoginRequest
import io.github.kartoffelsup.nuntius.api.user.result.FailedLogin
import io.github.kartoffelsup.nuntius.api.user.result.LoginResult
import io.github.kartoffelsup.nuntius.api.user.result.SuccessfulLogin
import io.github.kartoffelsup.nuntius.client.Failure
import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import io.github.kartoffelsup.nuntius.client.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.Align
import kotlinx.css.CSSBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.alignSelf
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.maxHeight
import kotlinx.css.maxWidth
import kotlinx.css.minWidth
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RHandler
import react.RProps
import react.ReactElement
import react.child
import react.dom.button
import react.dom.form
import react.dom.i
import react.dom.span
import react.functionalComponent
import react.useEffect
import react.useState
import kotlin.js.Promise

fun CSSBuilder.loginFormStyles() {
    +"login-form" {
        display = Display.flex
        flexDirection = FlexDirection.column
        maxHeight = LinearDimension("10vh")
        maxWidth = LinearDimension("30vw")
        justifyContent = JustifyContent.spaceBetween

        children {
            flex(1.0)
            margin(LinearDimension("5px"))
            lastChild {
                maxWidth = LinearDimension("50%")
                minWidth = LinearDimension("30%")
                alignSelf = Align.flexEnd
            }
        }
    }
}

data class FormSubmit(
    val email: String? = null,
    val password: String? = null,
    val submit: Boolean = false
)

private val loginForm = functionalComponent<LoginFormProps> { props ->
    val (formValid, setFormValid) = useState(false)
    val (emailValid, setEmailValid) = useState(false)
    val (passwordValid, setPasswordValid) = useState(false)
    val (formSubmit, setFormSubmit) = useState(FormSubmit())
    useEffect(listOf(formSubmit)) {
        val login: suspend () -> LoginResult = suspend {
            val mail = formSubmit.email!!
            val password = formSubmit.password!!
            val request = LoginRequest(mail, password)
            Promise.Companion.resolve("").await()
            val response =
                props.nuntiusApi.post<LoginRequest, SuccessfulLogin>(
                    "/user/login",
                    request,
                    LoginRequest.serializer(),
                    SuccessfulLogin.serializer()
                )
            when (response) {
                is Success<*> -> response.payload as SuccessfulLogin
                is Failure -> FailedLogin(response.reason)
            }
        }
        if (formSubmit.submit) {
            props.coroutineScope.launch {
                val loginResult: LoginResult = login()
                console.log(loginResult)
            }
        }
    }

    form(classes = "login-form") {
        formField {
            attrs {
                inputType = InputType.email
                name = "email"
                label = "E-Mail"
                classes = "form-field"
                validate = { value ->
                    when {
                        value.isEmpty() -> {
                            setEmailValid(false)
                            setFormValid(false)
                            "E-Mail is required"
                        }
                        !value.matches("(\\w+)@arml\\.com") -> {
                            setEmailValid(false)
                            setFormValid(false)
                            "E-Mail must end in @arml.com"
                        }
                        else -> {
                            setEmailValid(true)
                            setFormValid(passwordValid)
                            ""
                        }
                    }
                }
            }
        }
        formField {
            attrs {
                inputType = InputType.password
                name = "password"
                label = "Password"
                classes = "form-field"
                validate = { value ->
                    when {
                        value.isEmpty() -> {
                            setPasswordValid(false)
                            setFormValid(false)
                            "Password is required"
                        }
                        value.length < 5 -> {
                            setPasswordValid(false)
                            setFormValid(false)
                            "Password must at least be 5 characters"
                        }
                        else -> {
                            setPasswordValid(true)
                            setFormValid(emailValid)
                            ""
                        }
                    }
                }
            }
        }
        button(classes = "form-submit mdc-button mdc-button--unelevated demo-button-shaped", type = ButtonType.button) {
            attrs {
                disabled = !formValid
                onClickFunction = {
                    setFormSubmit(FormSubmit("email", "password", true))
                }
            }
            span(classes = "mdc-button__ripple") {}
            i(classes = "material-icons mdc-button__icon") { +"navigation" }
            span(classes = "mdc-button__label") { +"Submit" }
        }
    }
}

external interface LoginFormProps : RProps {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
}

fun RBuilder.loginForm(
    coroutineScope: CoroutineScope,
    nuntiusApi: NuntiusApiService, handler: RHandler<LoginFormProps>
): ReactElement {
    return child(functionalComponent = loginForm) {
        attrs.nuntiusApi = nuntiusApi
        attrs.coroutineScope = coroutineScope
        handler()
    }
}
