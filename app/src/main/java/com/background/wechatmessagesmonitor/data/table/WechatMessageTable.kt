package com.background.wechatmessagesmonitor.data.table

import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.PrimaryKey
import com.nice.sqlite.core.ddl.plus

object WechatMessageTable: Table("wechat_messages") {

    val Id = IntColumn("id") + PrimaryKey(true)
    val Who = StringColumn("who")
    val Content = StringColumn("content")
    val Time = DatetimeColumn("time")
    val DeviceId = DatetimeColumn("device_id")
    val Phone = StringColumn("phone")
    val CommissionerId = StringColumn("commissioner_id")
    val From = StringColumn("from")
    val type = IntColumn("type")
    val index = IntColumn("index")

}