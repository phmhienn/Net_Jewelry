package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record ReplyRequest(@NotBlank @Size(max = 3000) String reply) {}
