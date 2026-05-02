package com.capgemini.cart.dto.response;

import lombok.Data;

@Data
public class BookResponse {

 private Integer bookId;
 private String title;
 private Double price;
 private Integer stock;
 private String coverImageUrl;

}