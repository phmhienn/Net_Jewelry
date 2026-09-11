package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record LoginResponse(
    String accessToken, String tokenType, long expiresIn, UserResponse user) {}
