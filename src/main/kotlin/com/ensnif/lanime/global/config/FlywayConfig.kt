package com.ensnif.lanime.global.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig(
    @Value("\${db.migration-mode:migrate}") private val migrationMode: String,
    @Value("\${spring.datasource.url}") private val datasourceUrl: String,
    @Value("\${spring.datasource.username}") private val datasourceUsername: String,
    @Value("\${spring.datasource.password}") private val datasourcePassword: String
) {

    private val log = LoggerFactory.getLogger(FlywayConfig::class.java)

    @Bean
    fun flyway(): Flyway {
        val flyway = Flyway.configure()
            .dataSource(datasourceUrl, datasourceUsername, datasourcePassword)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .cleanDisabled(false)
            .load()

        when (migrationMode.lowercase().trim()) {
            "validate" -> {
                log.info("[Flyway] Mode: VALIDATE — 스키마 검증만 수행합니다.")
                flyway.validate()
            }
            "create" -> {
                log.warn("[Flyway] Mode: CREATE — DB를 초기화하고 마이그레이션을 재실행합니다. (데이터 삭제)")
                flyway.clean()
                flyway.migrate()
            }
            "migrate" -> {
                log.info("[Flyway] Mode: MIGRATE — 신규 마이그레이션 파일을 적용합니다.")
                flyway.repair()
                flyway.migrate()
            }
            else -> {
                log.warn("[Flyway] 알 수 없는 migration-mode '{}' — 기본값(migrate)으로 실행합니다.", migrationMode)
                flyway.repair()
                flyway.migrate()
            }
        }

        return flyway
    }
}
