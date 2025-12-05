package com.team.LetsStudyNow_rg.domain.openstudy;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


//StudyField Enum과 DB 간의 변환을 담당하는 Converter
@Converter(autoApply = true)
public class StudyFieldConverter implements AttributeConverter<StudyField, String> {


     //Enum → DB 저장 시: Enum의 한글 설명을 DB에 저장
    @Override
    public String convertToDatabaseColumn(StudyField attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDescription();
    }


    //DB → Enum 변환 시: DB의 한글 설명을 Enum으로 변환
    @Override
    public StudyField convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return StudyField.fromDescription(dbData);
    }
}
