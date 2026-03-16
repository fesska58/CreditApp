package ru.fess.deal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.fess.deal.dto.LoanOfferDto;

@Converter
public class LoanOfferDtoConverter implements AttributeConverter<LoanOfferDto, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(LoanOfferDto attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting LoanOfferDto to JSON", e);
        }
    }

    @Override
    public LoanOfferDto convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, LoanOfferDto.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to LoanOfferDto", e);
        }
    }
}
