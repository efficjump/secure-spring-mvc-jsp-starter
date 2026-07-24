package com.example.webstarter.localization;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportedLocaleRepository extends JpaRepository<SupportedLocale, Long> {

    List<SupportedLocale> findAllByOrderByDisplayOrderAscIdAsc();

    List<SupportedLocale> findAllByEnabledTrueOrderByDisplayOrderAscIdAsc();

    boolean existsByLanguageTagIgnoreCase(String languageTag);

    boolean existsByLanguageTagIgnoreCaseAndIdNot(String languageTag, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from SupportedLocale l where l.id = :id")
    Optional<SupportedLocale> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from SupportedLocale l order by l.id")
    List<SupportedLocale> findAllForUpdate();
}
