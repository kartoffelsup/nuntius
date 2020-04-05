package form

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onBlurFunction
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RHandler
import react.RProps
import react.ReactElement
import react.child
import react.dom.div
import react.dom.input
import react.dom.label
import react.functionalComponent
import react.useState

external interface FormFieldProps : RProps {
    var name: String
    var inputType: InputType
    var label: String
    var classes: String?
    var validate: (String) -> String
}

private val formField = functionalComponent<FormFieldProps> { props ->
    val (value, setValue) = useState("")
    val (focused, setFocused) = useState(false)
    val (validationMessage: String, setValidationMessage: (value: String) -> Unit) = useState("")
    val isValid = validationMessage.isEmpty()

    div(classes = "${props.classes ?: ""} mdc-text-field mdc-text-field--outlined ${if (focused) "mdc-text-field--focused" else ""} ${if (isValid) "" else "mdc-text-field--invalid"}") {
        input(name = props.name, classes = "mdc-text-field__input", type = props.inputType) {
            attrs {
                id = props.name
                onChangeFunction = { event ->
                    setValue((event.target as HTMLInputElement).value)
                    setValidationMessage(props.validate((event.target as HTMLInputElement).value))
                }
                onFocusFunction = {
                    setFocused(true)
                }
                onBlurFunction = {
                    setFocused(false)
                }
                required = true
            }
        }
        div(classes = "mdc-notched-outline ${if (value.isNotEmpty() || focused) "mdc-notched-outline--notched" else ""}") {
            div(classes = "mdc-notched-outline__leading") {}
            div(classes = "mdc-notched-outline__notch ${if (focused) "mdc-text-field--focused" else ""}") {
                label(classes = "mdc-floating-label ${if (value.isNotEmpty() || focused) "mdc-floating-label--float-above" else ""} ") {
                    attrs {
                        htmlFor = props.name
                    }
                    +props.label
                }
            }
            div(classes = "mdc-notched-outline__trailing") {}
        }
    }
    div(classes = "mdc-text-field-helper-line") {
        div(classes = "mdc-text-field-helper-text mdc-text-field-helper-text--persistent mdc-text-field-helper-text--validation-msg") {
            +validationMessage
        }
    }
}

fun RBuilder.formField(handler: RHandler<FormFieldProps>): ReactElement {
    return child(formField, handler = handler)
}
