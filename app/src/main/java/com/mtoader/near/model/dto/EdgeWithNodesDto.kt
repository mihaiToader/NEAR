package com.mtoader.near.model.dto

data class EdgeWithNodesDto constructor(var from: NodeDto, var to: NodeDto, var deviceName: String, var sessionName: String)
