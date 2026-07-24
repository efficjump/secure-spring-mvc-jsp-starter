package com.example.webstarter.localization;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SupportedLocaleForm {

    @NotBlank
    @Size(max = 35)
    @Pattern(regexp = "[A-Za-z]{2,8}(?:-[A-Za-z0-9]{1,8})*")
    private String languageTag;

    @NotBlank
    @Size(max = 80)
    private String displayName;

    @NotBlank
    @Size(max = 80)
    private String nativeName;

    @Min(0)
    @Max(9999)
    private int displayOrder;

    private boolean enabled = true;

    public String getLanguageTag() {
        return languageTag;
    }

    public void setLanguageTag(String languageTag) {
        this.languageTag = languageTag == null ? null : languageTag.strip();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? null : displayName.strip();
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName == null ? null : nativeName.strip();
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
