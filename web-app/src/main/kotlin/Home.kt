import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RHandler
import react.RProps
import react.ReactElement
import react.child
import react.dom.button
import react.dom.div
import react.dom.span
import react.functionalComponent
import react.useState

private val home = functionalComponent<RProps> {
    val (count, setCount) = useState(1)
    div {
        span {
            +"You clicked $count times"
        }
        button {
            attrs {
                onClickFunction = {
                    setCount(count + 1)
                }
            }
        }
    }
}

fun RBuilder.home(handler: RHandler<RProps>): ReactElement {
    return child(functionalComponent = home, handler = handler)
}
