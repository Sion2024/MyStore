package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSchedulerService {

    private final OrderRepository orderRepository;

    /*
     * Scheduled job using cron expression to mark orders as delivered.
     * Runs every 2 minutes at second 0
     * Cron expression: "0 *{@literal /}2 * * * *" = second, minute, hour, day, month, weekday
     */
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void markOrdersAsDeliveredCron() {
        log.info("Cron scheduled job started: Marking IN_TRANSIT and APPROVED orders as DELIVERED");
        
        try {
            List<Order> ordersToDeliver = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.IN_TRANSIT || order.getStatus() == OrderStatus.APPROVED)
                    .toList();
            
            if (ordersToDeliver.isEmpty()) {
                log.info("No orders with IN_TRANSIT or APPROVED status found");
                return;
            }
            
            int updatedCount = 0;
            for (Order order : ordersToDeliver) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                updatedCount++;
            }
            
            log.info("Cron scheduled job completed: {} orders marked as DELIVERED", updatedCount);
        } catch (Exception e) {
            log.error("Error in cron scheduled job while marking orders as delivered", e);
        }
    }

    /*
     * Scheduled job using fixedRate to mark orders as delivered.
     * Runs every 2 minutes (120000 milliseconds = 2 minutes)
     * Fixed rate means it runs at fixed intervals regardless of execution time
     */
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void markOrdersAsDeliveredFixedRate() {
        log.info("FixedRate scheduled job started: Marking IN_TRANSIT and APPROVED orders as DELIVERED");
        
        try {
            List<Order> ordersToDeliver = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.IN_TRANSIT || order.getStatus() == OrderStatus.APPROVED)
                    .toList();
            
            if (ordersToDeliver.isEmpty()) {
                log.info("No orders with IN_TRANSIT or APPROVED status found");
                return;
            }
            
            int updatedCount = 0;
            for (Order order : ordersToDeliver) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                updatedCount++;
            }
            
            log.info("FixedRate scheduled job completed: {} orders marked as DELIVERED", updatedCount);
        } catch (Exception e) {
            log.error("Error in fixedRate scheduled job while marking orders as delivered", e);
        }
    }
}

