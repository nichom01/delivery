package com.deliverysystem.repository;

import com.deliverysystem.domain.PostcodeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostcodeRuleRepository extends JpaRepository<PostcodeRule, String> {
    List<PostcodeRule> findByRouteDepotId(String depotId);
    
    @Query("SELECT pr FROM PostcodeRule pr WHERE pr.route.depot.id = :depotId " +
           "AND (:date BETWEEN pr.effectiveFrom AND COALESCE(pr.effectiveTo, :date)) " +
           "ORDER BY CASE pr.level " +
           "WHEN 'FULL' THEN 1 " +
           "WHEN 'SECTOR' THEN 2 " +
           "WHEN 'DISTRICT' THEN 3 " +
           "WHEN 'AREA' THEN 4 " +
           "WHEN 'LETTER' THEN 5 END")
    List<PostcodeRule> findActiveRulesByDepot(@Param("depotId") String depotId, @Param("date") LocalDate date);
    
    @Query("SELECT pr FROM PostcodeRule pr WHERE " +
           "(:date BETWEEN pr.effectiveFrom AND COALESCE(pr.effectiveTo, :date)) " +
           "AND :pattern LIKE CONCAT(pr.pattern, '%') " +
           "ORDER BY CASE pr.level " +
           "WHEN 'FULL' THEN 1 " +
           "WHEN 'SECTOR' THEN 2 " +
           "WHEN 'DISTRICT' THEN 3 " +
           "WHEN 'AREA' THEN 4 " +
           "WHEN 'LETTER' THEN 5 END, " +
           "LENGTH(pr.pattern) DESC")
    List<PostcodeRule> findMatchingRules(@Param("pattern") String pattern, @Param("date") LocalDate date);
}
