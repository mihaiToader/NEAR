package com.mtoader.near.model.message

class MemberData(val name: String, val color: String, var id: String?) {

    override fun toString(): String {
        return "MemberData{" +
                "name='" + name + '\''.toString() +
                ", color='" + color + '\''.toString() +
                '}'.toString()
    }
}