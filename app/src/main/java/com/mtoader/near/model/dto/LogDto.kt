package com.mtoader.near.model.dto

import java.time.LocalDateTime

data class LogDto constructor(var created: String,
                              val type: LogType,
                              val origin: String,
                              val message: String,
                              var session: SessionDto,
                              var device: DeviceDto)