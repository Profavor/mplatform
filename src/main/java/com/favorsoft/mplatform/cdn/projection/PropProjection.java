package com.favorsoft.mplatform.cdn.projection;

import com.favorsoft.mplatform.cdn.domain.Prop;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "prop", types = { Prop.class })
public interface PropProjection {
    String getPropId();
    MgroupProjection getMgroup();
}