package com.marykatekitchen.mykitchen_agent.dto;

public class GroceryItem {

    private String name;
    private Double requiredQuantity;
    private Double availableQuantity;
    private Double missingQuantity;
    private String unit;

    public GroceryItem(String name, Double requiredQuantity, Double availableQuantity, Double missingQuantity, String unit) {
        this.name = name;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
        this.missingQuantity = missingQuantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public Double getRequiredQuantity() {
        return requiredQuantity;
    }

    public Double getAvailableQuantity() {
        return availableQuantity;
    }

    public Double getMissingQuantity() {
        return missingQuantity;
    }

    public String getUnit() {
        return unit;
    }
}