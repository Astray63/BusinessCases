package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.ChargingStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service responsible for calculating prices for reservations
 */
@Service
public class PriceCalculator {

    private static final Logger logger = LoggerFactory.getLogger(PriceCalculator.class);
    private static final int PRICE_SCALE = 2;
    private static final RoundingMode PRICE_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calculates the total price for a reservation based on duration and charging station rate
     *
     * @param station   The charging station
     * @param dateDebut Start date and time
     * @param dateFin   End date and time
     * @return Total price rounded to 2 decimal places
     */
    public BigDecimal calculateTotalPrice(ChargingStation station, LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (station == null || station.getPrixALaMinute() == null) {
            logger.warn("Cannot calculate price: station or price per minute is null");
            return BigDecimal.ZERO;
        }

        if (dateDebut == null || dateFin == null) {
            logger.warn("Cannot calculate price: dates are null");
            return BigDecimal.ZERO;
        }

        if (!dateDebut.isBefore(dateFin)) {
            logger.warn("Cannot calculate price: start date is not before end date");
            return BigDecimal.ZERO;
        }

        long minutes = ChronoUnit.MINUTES.between(dateDebut, dateFin);
        BigDecimal totalPrice = station.getPrixALaMinute()
                .multiply(BigDecimal.valueOf(minutes))
                .setScale(PRICE_SCALE, PRICE_ROUNDING);

        logger.debug("Calculated price for {} minutes at {} per minute: {}", 
                minutes, station.getPrixALaMinute(), totalPrice);

        return totalPrice;
    }

    /**
     * Calculates the duration in minutes between two dates
     *
     * @param dateDebut Start date and time
     * @param dateFin   End date and time
     * @return Duration in minutes
     */
    public long calculateDurationInMinutes(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(dateDebut, dateFin);
    }

    /**
     * Calculates the duration in hours between two dates
     *
     * @param dateDebut Start date and time
     * @param dateFin   End date and time
     * @return Duration in hours (rounded down)
     */
    public long calculateDurationInHours(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(dateDebut, dateFin);
    }

    /**
     * Formats a price as a string with currency symbol
     *
     * @param price The price to format
     * @return Formatted price string (e.g., "12.50 €")
     */
    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00 €";
        }
        return price.setScale(PRICE_SCALE, PRICE_ROUNDING).toPlainString() + " €";
    }

    /**
     * Calculates price with a discount percentage
     *
     * @param originalPrice  The original price
     * @param discountPercent Discount percentage (e.g., 10 for 10%)
     * @return Discounted price
     */
    public BigDecimal applyDiscount(BigDecimal originalPrice, int discountPercent) {
        if (originalPrice == null || discountPercent <= 0 || discountPercent >= 100) {
            return originalPrice;
        }

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100), PRICE_SCALE, PRICE_ROUNDING)
        );

        return originalPrice.multiply(discountMultiplier).setScale(PRICE_SCALE, PRICE_ROUNDING);
    }
}
