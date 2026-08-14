package com.marykatekitchen.mykitchen_agent.dto;

public record ProductLookupResponse(
    String barcode,
    String name,
    String brand,
    String quantity,
    String imageUrl,
    boolean found
) {}
