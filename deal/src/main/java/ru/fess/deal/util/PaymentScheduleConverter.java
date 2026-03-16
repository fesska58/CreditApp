package ru.fess.deal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.fess.deal.dto.PaymentScheduleElementDto;

import java.util.List;

@Converter
public class PaymentScheduleConverter implements AttributeConverter<List<PaymentScheduleElementDto>, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<PaymentScheduleElementDto> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting paymentSchedule to JSON", e);
        }
    }

    @Override
    public List<PaymentScheduleElementDto> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(
                    dbData,
                    new TypeReference<List<PaymentScheduleElementDto>>() {}
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to paymentSchedule", e);
        }
    }
}
