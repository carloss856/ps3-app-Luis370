package com.example.inventappluis370.domain

import com.example.inventappluis370.data.model.RbacResponse

/**
 * Permisos basados en RBAC del backend (deny-by-default).
 *
 * NOTA: no confundir moduleKey (stats/rbac) con strings de UI.
 */
object RbacPermissionManager {

    fun can(rbac: RbacResponse?, moduleKey: String, action: String): Boolean {
        if (rbac == null) return false
        val actions = rbac.modules[moduleKey] ?: return false
        return actions.any { it.equals(action, ignoreCase = true) }
    }

    fun canIndex(rbac: RbacResponse?, moduleKey: String): Boolean = can(rbac, moduleKey, "index")
    fun canShow(rbac: RbacResponse?, moduleKey: String): Boolean = can(rbac, moduleKey, "show")
    fun canStore(rbac: RbacResponse?, moduleKey: String): Boolean = can(rbac, moduleKey, "store")
    fun canUpdate(rbac: RbacResponse?, moduleKey: String): Boolean = can(rbac, moduleKey, "update")
    fun canDestroy(rbac: RbacResponse?, moduleKey: String): Boolean = can(rbac, moduleKey, "destroy")
}
