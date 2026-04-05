package com.ensnif.lanime.global.config

import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@ReadingConverter
class EncodingStatusReadConverter : Converter<String, EncodingStatus> {
    override fun convert(source: String): EncodingStatus = EncodingStatus.valueOf(source)
}

@WritingConverter
class EncodingStatusWriteConverter : Converter<EncodingStatus, String> {
    override fun convert(source: EncodingStatus): String = source.name
}
