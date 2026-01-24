package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.ValueObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Value object representing product attributes (e.g., color, size options).
 */
public final class ProductAttributes implements ValueObject {

    private final Map<String, String> attributes;

    private ProductAttributes(Map<String, String> attributes) {
        this.attributes = attributes != null
            ? Collections.unmodifiableMap(new HashMap<>(attributes))
            : Collections.emptyMap();
    }

    public static ProductAttributes of(Map<String, String> attributes) {
        return new ProductAttributes(attributes);
    }

    public static ProductAttributes empty() {
        return new ProductAttributes(null);
    }

    public Map<String, String> getAll() {
        return attributes;
    }

    public String get(String key) {
        return attributes.get(key);
    }

    public boolean has(String key) {
        return attributes.containsKey(key);
    }

    public Set<String> keys() {
        return attributes.keySet();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public int size() {
        return attributes.size();
    }

    public ProductAttributes with(String key, String value) {
        Map<String, String> newAttributes = new HashMap<>(attributes);
        newAttributes.put(key, value);
        return new ProductAttributes(newAttributes);
    }

    public ProductAttributes without(String key) {
        Map<String, String> newAttributes = new HashMap<>(attributes);
        newAttributes.remove(key);
        return new ProductAttributes(newAttributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAttributes that = (ProductAttributes) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
