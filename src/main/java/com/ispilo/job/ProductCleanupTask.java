package com.ispilo.job;

import com.ispilo.model.entity.Product;
import com.ispilo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCleanupTask {

    private final ProductRepository productRepository;

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1 AM
    @Transactional
    public void cleanupExpiredProducts() {
        log.info("Starting scheduled cleanup of expired products...");
        LocalDateTime now = LocalDateTime.now();
        List<Product> expiredProducts = productRepository.findByExpiresAtBefore(now);

        if (expiredProducts.isEmpty()) {
            log.info("No expired products found.");
            return;
        }

        log.info("Found {} expired products. Deleting...", expiredProducts.size());
        productRepository.deleteAll(expiredProducts);
        log.info("Successfully deleted expired products.");
    }
}