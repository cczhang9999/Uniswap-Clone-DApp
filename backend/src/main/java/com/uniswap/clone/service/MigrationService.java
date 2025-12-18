package com.uniswap.clone.service;

import com.uniswap.clone.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MigrationService {

    /**
     * Double Write Strategy for Migration
     * Use this when migrating from Old DB to New Sharded DB.
     */
    public void doubleWrite(Order order) {
        // 1. Write to Old DB (Synchronous) - assumed to be handled by caller before this if primary
        // 2. Write to New DB (Async/MQ) to avoid impacting main flow latency
        writeToNewDbAsync(order);
    }

    @Async
    public void writeToNewDbAsync(Order order) {
        try {
            // Logic to write to new sharded DB via a separate DataSource or Service
            log.info("Async writing order {} to new sharded DB...", order.getOrderId());
            // repositoryNew.save(order);
        } catch (Exception e) {
            log.error("Failed to double write order {}", order.getOrderId(), e);
            // Push to dead letter queue or retry log
        }
    }

    public void compareAndReconcile() {
        log.info("Starting reconciliation job...");
        // Logic to compare records between old and new DB
    }
}
