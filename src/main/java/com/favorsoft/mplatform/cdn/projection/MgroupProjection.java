package com.favorsoft.mplatform.cdn.projection;

import com.favorsoft.mplatform.cdn.domain.Mgroup;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "mgroup", types = { Mgroup.class })
public interface MgroupProjection {
    String getGroupId();
    MsectionProjection getMsection();
}