package view.register

import arrow.core.Either
import csstype.ClassName
import io.github.kartoffelsup.nuntius.api.user.result.CreateUserResult
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
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val submit: Boolean = false
)

external interface RegisterFormProps : Props {
    var coroutineScope: CoroutineScope
    var userService: UserService
}

val RegisterForm = FC<RegisterFormProps> { props ->
    val (formValid, setFormValid) = useState(false)
    val (usernameValid, setUsernameValid) = useState(false)
    val (emailValid, setEmailValid) = useState(false)
    val (passwordValid, setPasswordValid) = useState(false)
    val (formSubmit, setFormSubmit) = useState(FormSubmit())
    val (signupError, setSignupError) = useState("")

    val createUser: suspend () -> Either<String, CreateUserResult> = suspend {
        val username = formSubmit.username!!
        val mail = formSubmit.email!!
        val password = formSubmit.password!!
        props.userService.signup(username, mail, password)
    }

    useEffect(listOf(formSubmit)) {
        var ignore: Boolean = false
        if (formSubmit.submit) {
            setSignupError("")
            props.coroutineScope.launch {
                val result = createUser()
                if (!ignore) {
                    result.fold({
                        setFormSubmit(formSubmit.copy(submit = false))
                        setSignupError(it)
                    }, {
                        println(it)
                    })
                }
            }
            cleanup { ignore = true }
        }
    }

    div {
        className = ClassName("main")
        form {
            className = ClassName("login-form")
            val (username, setUsername) = useState("")
            val (email, setEmail) = useState("")
            val (password, setPassword) = useState("")
            FormField {
                inputType = InputType.text
                name = "username"
                label = "Username"
                classes = "form-field"
                value = username
                setValue = setUsername
                validate = { value ->
                    when {
                        value.isEmpty() -> {
                            setUsernameValid(false)
                            setFormValid(false)
                            "Username is required"
                        }

                        else -> {
                            setUsernameValid(true)
                            setFormValid(emailValid && passwordValid)
                            ""
                        }
                    }
                }
            }
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
                            setFormValid(passwordValid && usernameValid)
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
                            setFormValid(emailValid && usernameValid)
                            ""
                        }
                    }
                }
            }
            button {
                className = ClassName("form-submit mdc-button mdc-button--unelevated demo-button-shaped")
                type = ButtonType.button
                disabled = !formValid// || formSubmit.submit
                onClick = {
                    setFormSubmit(FormSubmit(username, email, password, true))
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
            div {
                className = ClassName("form-login-error mdc-theme--error")
                +signupError
            }
        }
    }
}
