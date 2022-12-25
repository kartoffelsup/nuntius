package view.form

import csstype.ClassName
import react.FC
import react.Props
import react.StateSetter
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState

external interface FormFieldProps : Props {
    var name: String
    var inputType: InputType
    var label: String
    var classes: String?
    var validate: (String) -> String
    var value: String
    var setValue: StateSetter<String>
}

val FormField = FC<FormFieldProps> { props ->
    val (focused, setFocused) = useState(false)
    val (validationMessage: String, setValidationMessage: StateSetter<String>) = useState("")
    val isValid = validationMessage.isEmpty()

    div {
        className =
            ClassName("${props.classes ?: ""} mdc-text-field mdc-text-field--outlined ${if (focused) "mdc-text-field--focused" else ""} ${if (isValid) "" else "mdc-text-field--invalid"}")
        input {
            name = props.name
            className = ClassName("mdc-text-field__input")
            type = props.inputType
            id = props.name
            required = true
            onChange = { event ->
                props.setValue(event.target.value)
                setValidationMessage(props.validate(event.target.value))
            }
            onFocus = {
                setFocused(true)
            }
            onBlur = {
                setFocused(false)
            }
        }
        div {
            className =
                ClassName("mdc-notched-outline ${if (props.value.isNotEmpty() || focused) "mdc-notched-outline--notched" else ""}")
            div {
                className = ClassName("mdc-notched-outline__leading")
            }
            div {
                className = ClassName("mdc-notched-outline__notch ${if (focused) "mdc-text-field--focused" else ""}")
                label {
                    className =
                        ClassName("mdc-floating-label ${if (props.value.isNotEmpty() || focused) "mdc-floating-label--float-above" else ""} ")
                    htmlFor = props.name
                    +props.label
                }
            }
            div {
                className = ClassName("mdc-notched-outline__trailing")
            }
        }
    }
    div {
        className = ClassName("mdc-text-field-helper-line")
        div {
            className =
                ClassName("mdc-text-field-helper-text mdc-text-field-helper-text--persistent mdc-text-field-helper-text--validation-msg")
            +validationMessage
        }
    }
}
