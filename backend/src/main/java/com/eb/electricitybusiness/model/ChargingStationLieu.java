package com.eb.electricitybusiness.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "charging_station_lieu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStationLieu {
    
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borne_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChargingStation chargingStation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieu_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lieu lieu;
}