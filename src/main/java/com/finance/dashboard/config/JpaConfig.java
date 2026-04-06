package com.finance.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing    // required for @CreatedDate and @LastModifiedDate on entities
public class JpaConfig {}
