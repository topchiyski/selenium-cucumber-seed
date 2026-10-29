package com.example.e2e.data;

/**
 * A shopper's checkout details.
 *
 * @param firstName first name
 * @param lastName last name
 * @param postalCode postal code
 */
public record Customer(String firstName, String lastName, String postalCode) {
}
