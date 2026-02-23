package com.aibe.team2.global.converter;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.JsonConversionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter
public class JsonAttributeConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 1. Entity 필드를 DB 컬럼으로 변환(직렬화)
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if(attribute == null ||  attribute.isEmpty()) {
            return null;
        }

        try{
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e){
            throw new JsonConversionException(ErrorCode.COMMON_JSON_CONVERSION_ERROR);
        }
    }

    // 2. DB 컬럼을 Entity 필드로 변환(역직렬화)
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        try{
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e){
            throw new JsonConversionException(ErrorCode.COMMON_JSON_CONVERSION_ERROR);
        }
    }
}
