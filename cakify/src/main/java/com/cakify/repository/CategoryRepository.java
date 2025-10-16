package com.cakify.repository;

import com.cakify.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by name (case-insensitive)
    Optional<Category> findByNameIgnoreCase(String name);

    // Check if category exists by name (case-insensitive)
    boolean existsByNameIgnoreCase(String name);
}