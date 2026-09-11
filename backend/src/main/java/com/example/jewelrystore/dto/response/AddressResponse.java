package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record AddressResponse(
    Long id,
    String name,
    String phone,
    String city,
    String district,
    String ward,
    String street,
    boolean defaultAddress) {}
