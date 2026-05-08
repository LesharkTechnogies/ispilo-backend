package com.ispilo.service;

import com.ispilo.model.entity.BannedDevice;
import com.ispilo.repository.BannedDeviceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannedDeviceCacheService {

    private final BannedDeviceRepository bannedDeviceRepository;
    private final Set<String> bannedDeviceIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Scheduled(fixedDelayString = "${app.banned-devices.refresh-ms:300000}")
    public void refreshCache() {
        bannedDeviceIds.clear();
        for (BannedDevice device : bannedDeviceRepository.findAll()) {
            if (device.getDeviceId() != null && !device.getDeviceId().isBlank()) {
                bannedDeviceIds.add(device.getDeviceId());
            }
        }
        log.debug("Refreshed banned devices cache ({} entries)", bannedDeviceIds.size());
    }

    public boolean isBanned(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return false;
        }
        return bannedDeviceIds.contains(deviceId);
    }
}
