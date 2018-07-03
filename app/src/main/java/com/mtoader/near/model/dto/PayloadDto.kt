package com.mtoader.near.model.dto

data class PayloadDto constructor(var type: PayloadType,
                                  var destination: String,
                                  var commandType: String,
                                  var data: String,
                                  var deviceName: String,
                                  var sessionName: String)
