package com.restaurant.vip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration to enable AOP for auditing
 */
@Configuration
@EnableAspectJAutoProxy
public class AuditConfig {
    // AOP configuration is handled by @EnableAspectJAutoProxy
}