package htw.webtech.financeMaster.rest.dto;

public class RecentTransactionDto {
    private Long id;
    private String type;
    private Double amount;
    private String description;
    private String date;
    private String categoryName;

    public RecentTransactionDto() {}

    public RecentTransactionDto(Long id, String type, Double amount, String description, String date, String categoryName) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.categoryName = categoryName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
