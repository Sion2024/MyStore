package com.softuni.finalexam.repository;

import com.softuni.finalexam.models.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {


}
