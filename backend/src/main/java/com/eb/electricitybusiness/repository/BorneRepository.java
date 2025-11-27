package com.eb.electricitybusiness.repository;

import com.eb.electricitybusiness.model.Borne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorneRepository extends JpaRepository<Borne, Long> {
        @Query("SELECT cs FROM Borne cs LEFT JOIN FETCH cs.owner WHERE cs.etat = :etat")
        List<Borne> findByEtat(@Param("etat") Borne.Etat etat);

        @Query("SELECT cs FROM Borne cs LEFT JOIN FETCH cs.medias WHERE cs.owner.idUtilisateur = :ownerId")
        List<Borne> findByOwnerIdUtilisateur(@Param("ownerId") Long ownerId);

        @Query("SELECT cs FROM Borne cs LEFT JOIN FETCH cs.medias JOIN cs.lieu l WHERE l.idLieu = :idLieu")
        List<Borne> findByLieuxId(@Param("idLieu") Long idLieu);

        @Query(value = "SELECT * FROM borne cs " +
                        "WHERE cs.latitude IS NOT NULL AND cs.longitude IS NOT NULL " +
                        "AND (6371 * acos(" +
                        "cos(radians(:latitude)) * cos(radians(cs.latitude)) * cos(radians(cs.longitude) - radians(:longitude)) + "
                        +
                        "sin(radians(:latitude)) * sin(radians(cs.latitude))" +
                        ")) < :distance", nativeQuery = true)
        List<Borne> findByDistance(
                        @Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("distance") Double distance);
}