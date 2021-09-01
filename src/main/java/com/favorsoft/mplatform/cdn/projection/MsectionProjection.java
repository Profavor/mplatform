package com.favorsoft.mplatform.cdn.projection;

import com.favorsoft.mplatform.cdn.domain.Msection;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "msection", types = { Msection.class })
public interface MsectionProjection {
    String getSectionId();
}