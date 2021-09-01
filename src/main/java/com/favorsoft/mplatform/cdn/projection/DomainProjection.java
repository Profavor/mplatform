package com.favorsoft.mplatform.cdn.projection;

import com.favorsoft.mplatform.cdn.domain.Domain;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "domain", types = { Domain.class })
public interface DomainProjection {
    String getDomainId();
}