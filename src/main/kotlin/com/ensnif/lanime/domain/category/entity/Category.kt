package com.ensnif.lanime.domain.category.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("category")
data class Category(
    @Id
    val categoryId: UUID? = null,
    val name: String,   // 예: TV 시리즈, 극장판
    val code: String    // 예: TV, MOVIE
) : BaseEntity()