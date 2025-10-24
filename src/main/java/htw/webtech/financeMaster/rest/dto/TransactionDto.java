package htw.webtech.financeMaster.rest.dto;

public class TransactionDto {
    public Long id;
    public String type;
    public Double amount;
    public String description;
    public String date; // ISO string
    public Long categoryId;
    public String categoryName;

    public TransactionDto() {}
    public TransactionDto(Long id, String type, Double amount, String description, String date, Long categoryId, String categoryName) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}
