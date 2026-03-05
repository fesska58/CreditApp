package ru.fess.calculator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "calculator.rate")
public class CalculatorProperties {
    private BigDecimal base;
    private BigDecimal insuranceDiscount;
    private BigDecimal salaryDiscount;
}
