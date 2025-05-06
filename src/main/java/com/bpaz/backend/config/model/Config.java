package com.bpaz.backend.config.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app-config")
@Data
@NoArgsConstructor
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    @Column(name = "ENV")
    private String env;

    @Column(name = "DOMAIN")
    private String domain;

    @Column(name = "APP_CPU_REQUEST")
    private String appCpuRequest;

    @Column(name = "APP_MEMORY_REQUEST")
    private String appMemoryRequest;

    @Column(name = "APP_CPU_LIMIT")
    private String appCpuLimit;

    @Column(name = "APP_MEMORY_LIMIT")
    private String appMemoryLimit;

    @Column(name = "REPLICAS")
    private String replicas;

    @Column(name = "APAAS_ID")
    private String apaasId;
}
