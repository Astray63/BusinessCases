package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.model.Borne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service responsable du calcul des prix pour les réservations
 */
@Service
public class PriceCalculator {

    private static final Logger logger = LoggerFactory.getLogger(PriceCalculator.class);
    private static final int PRICE_SCALE = 2;
    private static final RoundingMode PRICE_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calcule le prix total d'une réservation basé sur la durée et le tarif de la
     * borne
     *
     * @param station   La borne de recharge
     * @param dateDebut Date et heure de début
     * @param dateFin   Date et heure de fin
     * @return Prix total arrondi à 2 décimales
     */
    public BigDecimal calculateTotalPrice(Borne borne, LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (borne == null || borne.getPrixALaMinute() == null) {
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
        BigDecimal totalPrice = borne.getPrixALaMinute()
                .multiply(BigDecimal.valueOf(minutes))
                .setScale(PRICE_SCALE, PRICE_ROUNDING);

        logger.debug("Calculated price for {} minutes at {} per minute: {}",
                minutes, borne.getPrixALaMinute(), totalPrice);

        return totalPrice;
    }

    /**
     * Calcule la durée en minutes entre deux dates
     *
     * @param dateDebut Date et heure de début
     * @param dateFin   Date et heure de fin
     * @return Durée en minutes
     */
    public long calculateDurationInMinutes(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(dateDebut, dateFin);
    }

    /**
     * Calcule la durée en heures entre deux dates
     *
     * @param dateDebut Date et heure de début
     * @param dateFin   Date et heure de fin
     * @return Durée en heures (arrondie à l'inférieur)
     */
    public long calculateDurationInHours(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(dateDebut, dateFin);
    }

    /**
     * Formate un prix en chaîne avec le symbole de devise
     *
     * @param price Le prix à formater
     * @return Chaîne de prix formatée (ex: "12.50 €")
     */
    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00 €";
        }
        return price.setScale(PRICE_SCALE, PRICE_ROUNDING).toPlainString() + " €";
    }

    /**
     * Calcule le prix avec un pourcentage de réduction
     *
     * @param originalPrice   Le prix original
     * @param discountPercent Pourcentage de réduction (ex: 10 pour 10%)
     * @return Prix réduit
     */
    public BigDecimal applyDiscount(BigDecimal originalPrice, int discountPercent) {
        if (originalPrice == null || discountPercent <= 0 || discountPercent >= 100) {
            return originalPrice;
        }

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100), PRICE_SCALE, PRICE_ROUNDING));

        return originalPrice.multiply(discountMultiplier).setScale(PRICE_SCALE, PRICE_ROUNDING);
    }
}
