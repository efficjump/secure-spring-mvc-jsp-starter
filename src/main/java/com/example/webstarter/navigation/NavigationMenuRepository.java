package com.example.webstarter.navigation;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NavigationMenuRepository extends JpaRepository<NavigationMenu, Long> {

    List<NavigationMenu> findAllByOrderByDisplayOrderAscIdAsc();

    List<NavigationMenu> findAllByEnabledTrueOrderByDisplayOrderAscIdAsc();

    boolean existsByMenuKey(String menuKey);

    boolean existsByMenuKeyAndIdNot(String menuKey, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from NavigationMenu m where m.id = :id")
    Optional<NavigationMenu> findByIdForUpdate(@Param("id") Long id);
}
