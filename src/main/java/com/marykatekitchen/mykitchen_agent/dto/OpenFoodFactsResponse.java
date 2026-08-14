package com.marykatekitchen.mykitchen_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenFoodFactsResponse(
    int status,
    OpenFoodFactsProduct product
) {}
