package com.classification.domain_system.context;

import java.time.ZoneId;

public final class UserTimeZoneContextHolder {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final ThreadLocal<ZoneId> CURRENT_ZONE_ID = new ThreadLocal<>();

    private UserTimeZoneContextHolder() {
    }

    public static void setZoneId(ZoneId zoneId) {
        if (zoneId != null) {
            CURRENT_ZONE_ID.set(zoneId);
        } else {
            CURRENT_ZONE_ID.remove();
        }
    }

    public static ZoneId getZoneId() {
        ZoneId zoneId = CURRENT_ZONE_ID.get();
        return zoneId != null ? zoneId : DEFAULT_ZONE_ID;
    }

    public static void clear() {
        CURRENT_ZONE_ID.remove();
    }
}
