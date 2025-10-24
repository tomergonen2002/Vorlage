package htw.webtech.financeMaster.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateTransactionRequest {
    @NotBlank
    private String type; // "Einnahme"/"Ausgabe" or "INCOME"/"EXPENSE"

    @NotNull
    @Positive
    private Double amount;

    private String description;

    // ISO-8601 yyyy-MM-dd
    private String date;

    @NotNull
    private Long categoryId;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
