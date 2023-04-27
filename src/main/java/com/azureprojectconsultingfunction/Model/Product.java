package com.azureprojectconsultingfunction.Model;

import lombok.Data;

@Data
public class Product {
    private String productId;
    private String product_type;
    private String title;
    private String brand;
    private String sku;
    private float price;
    private String currency;
    private String availability;
    private String item_condition;
    private String game_platform;
    private String operating_systems;
    private String images;
    private String sub_cateory;
    private String category;
    private String publisher;
    private String name;
    private String description;
}