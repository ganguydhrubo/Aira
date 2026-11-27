package com.ganguydhrubo.aira.shell

import androidx.annotation.Keep
import com.ganguydhrubo.aira.cap.phone.shell.ScreenInspect

/**
 * 只读读屏的入口:电脑端 CLI(`aira screen`)的命令里写的是这个全名,不随内部重构改名;实现在手机能力里。
 * `CLASSPATH=<base.apk> app_process /system/bin com.ganguydhrubo.aira.shell.ScreenInspectMain`
 */
@Keep
object ScreenInspectMain {
    @JvmStatic
    fun main(args: Array<String>) = ScreenInspect.main(args)
}
