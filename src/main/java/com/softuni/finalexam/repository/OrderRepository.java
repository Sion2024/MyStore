package com.softuni.finalexam.repository;

import com.softuni.finalexam.enums.OrderStatus;
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
    
    /**
     * Find all orders by status
     * @param status the order status to search for
     * @return list of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user")
    List<Order> findAllWithUsers();

}
