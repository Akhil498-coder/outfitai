package com.outfitai.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** PUT replaces all three fields: send null (or omit) age/gender to clear them. */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String displayName,
        @Min(13) @Max(120) Integer age,
        Gender gender) {
}
