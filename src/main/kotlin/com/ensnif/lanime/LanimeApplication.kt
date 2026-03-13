package com.ensnif.lanime

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LanimeApplication

fun main(args: Array<String>) {
	runApplication<LanimeApplication>(*args)
}
