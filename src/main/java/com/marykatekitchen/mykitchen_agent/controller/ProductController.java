package com.marykatekitchen.mykitchen_agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marykatekitchen.mykitchen_agent.dto.ProductLookupResponse;
import com.marykatekitchen.mykitchen_agent.service.ProductLookupService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductLookupService productLookupService;

    public ProductController(
        ProductLookupService productLookupService
    ) {
        this.productLookupService = productLookupService;
    }

    @GetMapping("/barcode/{barcode}")
    public ProductLookupResponse getByBarcode(
        @PathVariable String barcode
    ) {
        return productLookupService.findByBarcode(barcode);
    }
}
