package com.syncapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing an environment variable.
 */
@Entity
@Table(name = "environment_variables")
public class EnvironmentVariable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "var_key")
    private String key;

    @Column(nullable = false, name = "var_value", length = 2048)
    private String value;

    // many-to-one, variable belongs to one environment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    /**
     * Default constructor.
     */
    public EnvironmentVariable() {
    }

    /**
     * Parameterized constructor.
     *
     * @param key         the variable key
     * @param value       the variable value
     * @param environment the environment this variable belongs to
     */
    public EnvironmentVariable(String key, String value, Environment environment) {
        this.key = key;
        this.value = value;
        this.environment = environment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
