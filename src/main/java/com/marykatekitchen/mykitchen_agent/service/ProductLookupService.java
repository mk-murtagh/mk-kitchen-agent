package com.marykatekitchen.mykitchen_agent.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.marykatekitchen.mykitchen_agent.dto.OpenFoodFactsProduct;
import com.marykatekitchen.mykitchen_agent.dto.OpenFoodFactsResponse;
import com.marykatekitchen.mykitchen_agent.dto.ProductLookupResponse;

@Service
public class ProductLookupService {
    private final RestClient restClient;

    public ProductLookupService() {
        this.restClient = RestClient.builder()
            .baseUrl("https://world.openfoodfacts.org")
            .defaultHeader(
                "User-Agent",
                "MyKitchenAgent/1.0 (your-email@example.com)"
            )
            .build();
    }


    public ProductLookupResponse findByBarcode(String barcode) {
        OpenFoodFactsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/api/v2/product/{barcode}")
                        .queryParam(
                            "fields",
                            "code,product_name,brands,quantity,image_front_url"
                        )
                        .build(barcode)
                    )
                    .retrieve()
                    .body(OpenFoodFactsResponse.class);

        if (response == null
                || response.status() == 0
                || response.product() == null) {

            return new ProductLookupResponse(
                barcode,
                null,
                null,
                null,
                null,
                false
            );
        }

        OpenFoodFactsProduct product = response.product();

        return new ProductLookupResponse(
            barcode,
            product.productName(),
            product.brands(),
            product.quantity(),
            product.imageFrontUrl(),
            true
        );
    }
}