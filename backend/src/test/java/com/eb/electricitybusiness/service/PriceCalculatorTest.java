package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Borne;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    private PriceCalculator priceCalculator;

    @BeforeEach
    void setUp() {
        priceCalculator = new PriceCalculator();
    }

    @Test
    void calculateTotalPrice_ValidInputs_ReturnsCorrectPrice() {
        Borne borne = new Borne();
        borne.setPrixALaMinute(new BigDecimal("0.50"));

        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 30); // 90 minutes

        BigDecimal result = priceCalculator.calculateTotalPrice(borne, start, end);

        // 90 * 0.50 = 45.00
        assertEquals(new BigDecimal("45.00"), result);
    }

    @Test
    void calculateTotalPrice_StationArgumentsNull_ReturnsZero() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(10);

        assertEquals(BigDecimal.ZERO, priceCalculator.calculateTotalPrice(null, start, end));

        Borne b = new Borne(); // Prix null
        assertEquals(BigDecimal.ZERO, priceCalculator.calculateTotalPrice(b, start, end));
    }

    @Test
    void calculateTotalPrice_DateArgumentsNull_ReturnsZero() {
        Borne borne = new Borne();
        borne.setPrixALaMinute(BigDecimal.TEN);

        assertEquals(BigDecimal.ZERO, priceCalculator.calculateTotalPrice(borne, null, LocalDateTime.now()));
        assertEquals(BigDecimal.ZERO, priceCalculator.calculateTotalPrice(borne, LocalDateTime.now(), null));
    }

    @Test
    void calculateTotalPrice_InvalidDates_ReturnsZero() {
        Borne borne = new Borne();
        borne.setPrixALaMinute(BigDecimal.TEN);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusMinutes(10); // End before start

        assertEquals(BigDecimal.ZERO, priceCalculator.calculateTotalPrice(borne, start, end));
    }

    @Test
    void calculateDurationInMinutes_Valid_ReturnsMinutes() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 10, 30);
        assertEquals(30, priceCalculator.calculateDurationInMinutes(start, end));
    }

    @Test
    void calculateDurationInMinutes_Null_ReturnsZero() {
        assertEquals(0, priceCalculator.calculateDurationInMinutes(null, LocalDateTime.now()));
    }

    @Test
    void calculateDurationInHours_Valid_ReturnsHours() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 12, 0);
        assertEquals(2, priceCalculator.calculateDurationInHours(start, end));
    }

    @Test
    void calculateDurationInHours_Null_ReturnsZero() {
        assertEquals(0, priceCalculator.calculateDurationInHours(null, LocalDateTime.now()));
    }

    @Test
    void formatPrice_Valid_ReturnsFormattedString() {
        assertEquals("12.50 €", priceCalculator.formatPrice(new BigDecimal("12.50")));
        assertEquals("10.00 €", priceCalculator.formatPrice(new BigDecimal("10")));
    }

    @Test
    void formatPrice_Null_ReturnsZeroString() {
        assertEquals("0.00 €", priceCalculator.formatPrice(null));
    }

    @Test
    void applyDiscount_Valid_ReturnsDiscountedPrice() {
        // 100 - 20% = 80
        assertEquals(new BigDecimal("80.00"),
                priceCalculator.applyDiscount(new BigDecimal("100"), 20));
    }

    @Test
    void applyDiscount_InvalidInputs_ReturnsOriginalPrice() {
        assertEquals(new BigDecimal("100"),
                priceCalculator.applyDiscount(new BigDecimal("100"), 0)); // 0%

        assertEquals(new BigDecimal("100"),
                priceCalculator.applyDiscount(new BigDecimal("100"), 100)); // 100% (or more maybe treated as invalid in
                                                                            // logic?)

        // Logic says: if discountPercent <= 0 || discountPercent >= 100
        assertEquals(new BigDecimal("100"),
                priceCalculator.applyDiscount(new BigDecimal("100"), 120));

        assertNull(priceCalculator.applyDiscount(null, 20));
    }
}
