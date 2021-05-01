package view.login

import io.github.kartoffelsup.nuntius.api.user.result.FailedLogin
import io.github.kartoffelsup.nuntius.api.user.result.LoginResult
import io.github.kartoffelsup.nuntius.api.user.result.SuccessfulLogin
import jsonx
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.css.CSSBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
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
import react.dom.div
import react.dom.form
import react.dom.i
import react.dom.span
import react.functionalComponent
import react.useEffect
import react.useState
import service.user.UserService
import view.form.formField

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
        }

        child(".form-submit") {
            maxWidth = LinearDimension("50%")
            minWidth = LinearDimension("30%")

        }

        child(".form-login-error") {
            minWidth = LinearDimension("30%")
            put("color", "var(--mdc-theme-error)")
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
    val (loginError, setLoginError) = useState("")
    useEffect(listOf(formSubmit)) {
        val login: suspend () -> LoginResult = suspend {
            val mail = formSubmit.email!!
            val password = formSubmit.password!!
            props.userService.login(mail, password)
        }
        if (formSubmit.submit) {
            setLoginError("")
            props.coroutineScope.launch {
                when (val result = login()) {
                    is SuccessfulLogin -> {
                        localStorage.setItem("nuntius-user", jsonx.encodeToString(SuccessfulLogin.serializer(), result))
                    }
                    is FailedLogin -> {
                        setFormSubmit(formSubmit.copy(submit = false))
                        setLoginError(result.message)
                    }
                }
            }
        }
    }

    form(classes = "login-form") {
        val (email, setEmail) = useState("")
        val (password, setPassword) = useState("")
        formField {
            attrs {
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
        }
        div(classes = "form-login-error mdc-theme--error") {
            +loginError
        }
        button(
            classes = "form-submit mdc-button mdc-button--unelevated demo-button-shaped",
            type = ButtonType.button
        ) {
            attrs {
                disabled = !formValid || formSubmit.submit
                onClickFunction = {
                    setFormSubmit(FormSubmit(email, password, true))
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
    var userService: UserService
}

fun RBuilder.loginForm(
    coroutineScope: CoroutineScope,
    userService: UserService, handler: RHandler<LoginFormProps>
): ReactElement {
    return child(loginForm) {
        attrs.userService = userService
        attrs.coroutineScope = coroutineScope
        handler()
    }
}
