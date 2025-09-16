package com.zzan.zzan

import org.springframework.boot.fromApplication


fun main(args: Array<String>) {
    fromApplication<ZzanApplication>().run(*args)
}
