package com.favorsoft.mplatform.cdn.projection;

import com.favorsoft.mplatform.cdn.domain.ClassProp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "classProp", types = { ClassProp.class })
public interface ClassPropProjection {
    @Value("#{target.getDomainId()}")
    String getDomainId();
    @Value("#{target.getClassId()}")
    String getClassId();
    @Value("#{target.getClassPropKey().getProp()}")
    PropProjection getProp();
}