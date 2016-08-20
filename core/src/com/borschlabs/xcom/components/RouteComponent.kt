package com.borschlabs.xcom.components

import com.badlogic.ashley.core.Component
import com.borschlabs.xcom.world.FieldCell

/**
 * @author octopussy
 */

class RouteComponent : Component {

    var fromCell: FieldCell? = null
    var toCell: FieldCell? = null
}