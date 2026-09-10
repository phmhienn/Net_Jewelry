package com.example.jewelrystore.dto.request;

import jakarta.validation.constraints.NotNull;

public record SelectionRequest(@NotNull Boolean selected) {}
