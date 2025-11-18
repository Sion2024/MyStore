package com.softuni.finalexam.enums;

public enum EmailType {
    WELCOME("welcome-email"),
    NEWSLETTER("newsletter-email"),
    ORDER_CONFIRMATION("order-confirmation-email"),
    NEW_ORDER("new-order-email"),
    SHIPPED_ORDER("shipped-order-email");

    private final String template;

    EmailType(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}

