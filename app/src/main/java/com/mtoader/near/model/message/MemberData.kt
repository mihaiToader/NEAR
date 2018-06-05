package com.mtoader.near.model.message

class MemberData(val name: String, val color: String) {

    override fun toString(): String {
        return "MemberData{" +
                "name='" + name + '\''.toString() +
                ", color='" + color + '\''.toString() +
                '}'.toString()
    }
}