package com.softuni.finalexam.repository;

import com.softuni.finalexam.models.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find all orders with eagerly loaded user data (for admin use only)
     * Uses JOIN FETCH to prevent lazy loading issues when accessing user information
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user")
    List<Order> findAllWithUsers();

}
