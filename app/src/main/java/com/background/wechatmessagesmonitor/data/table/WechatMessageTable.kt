package com.background.wechatmessagesmonitor.data.table

import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.PrimaryKey
import com.nice.sqlite.core.ddl.plus

object WechatMessageTable: Table("wechat_messages") {

    val Id = IntColumn("id") + PrimaryKey(true)

}