package view.login

import csstype.ClassName
import io.github.kartoffelsup.nuntius.api.user.result.FailedLogin
import io.github.kartoffelsup.nuntius.api.user.result.LoginResult
import io.github.kartoffelsup.nuntius.api.user.result.SuccessfulLogin
import jsonx
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ButtonType
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.useEffect
import react.useState
import service.user.UserService
import view.form.FormField

data class FormSubmit(
    val email: String? = null,
    val password: String? = null,
    val submit: Boolean = false
)

external interface LoginFormProps : Props {
    var coroutineScope: CoroutineScope
    var userService: UserService
}

val LoginForm = FC<LoginFormProps> { props ->
    val (formValid, setFormValid) = useState(false)
    val (emailValid, setEmailValid) = useState(false)
    val (passwordValid, setPasswordValid) = useState(false)
    val (formSubmit, setFormSubmit) = useState(FormSubmit())
    val (loginError, setLoginError) = useState("")

    val login: suspend () -> LoginResult = suspend {
        val mail = formSubmit.email!!
        val password = formSubmit.password!!
        props.userService.login(mail, password)
    }

    useEffect(listOf(formSubmit)) {
        var ignore: Boolean = false
        if (formSubmit.submit) {
            setLoginError("")
            props.coroutineScope.launch {
                val result = login()
                if (!ignore) {
                    when (result) {
                        is SuccessfulLogin -> {
                            localStorage.setItem(
                                "nuntius-user",
                                jsonx.encodeToString(SuccessfulLogin.serializer(), result)
                            )
                        }

                        is FailedLogin -> {
                            setFormSubmit(formSubmit.copy(submit = false))
                            setLoginError(result.message)
                        }
                    }
                }
            }
            cleanup { ignore = true }
        }
    }

    div {
        className = ClassName("main")
        form {
            className = ClassName("login-form")
            val (email, setEmail) = useState("")
            val (password, setPassword) = useState("")
            FormField {
                inputType = InputType.email
                name = "email"
                label = "E-Mail"
                classes = "form-field"
                value = email
                setValue = setEmail
                validate = { value ->
                    when {
                        value.isEmpty() -> {
                            setEmailValid(false)
                            setFormValid(false)
                            "E-Mail is required"
                        }

                        !Regex("(\\w+)@arml\\.com").matches(value) -> {
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
            FormField {
                inputType = InputType.password
                name = "password"
                label = "Password"
                classes = "form-field"
                value = password
                setValue = setPassword
                validate = { value ->
                    when {
                        value.isEmpty() -> {
                            setPasswordValid(false)
                            setFormValid(false)
                            "Password is required"
                        }

                        value.length < 4 -> {
                            setPasswordValid(false)
                            setFormValid(false)
                            "Password must at least be 4 characters"
                        }

                        else -> {
                            setPasswordValid(true)
                            setFormValid(emailValid)
                            ""
                        }
                    }
                }
            }
            div {
                className = ClassName("form-login-error mdc-theme--error")
                +loginError
            }
            button {
                className = ClassName("form-submit mdc-button mdc-button--unelevated demo-button-shaped")
                type = ButtonType.button
                disabled = !formValid// || formSubmit.submit
                onClick = {
                    console.log("onClick")
                    setFormSubmit(FormSubmit(email, password, true))
                }
                span {
                    className = ClassName("mdc-button__ripple")
                }
                i {
                    className = ClassName("material-icons mdc-button__icon")
                    +"navigation"
                }
                span {
                    className = ClassName("mdc-button__label")
                    +"Submit"
                }
            }
        }
    }
}
