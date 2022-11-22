package view

import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useState


val Home = FC<Props> {
    val (count, setCount) = useState(1)
    div {
        className = ClassName("main")
        span {
            +"You clicked $count times"
        }
        button {
            onClick = {
                setCount(count + 1)
            }
        }
    }
}
