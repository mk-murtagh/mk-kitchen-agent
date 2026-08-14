package com.marykatekitchen.mykitchen_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenFoodFactsProduct(

    @JsonProperty("product_name")
    String productName,

    String brands,

    String quantity,

    @JsonProperty("image_front_url")
    String imageFrontUrl

) {}