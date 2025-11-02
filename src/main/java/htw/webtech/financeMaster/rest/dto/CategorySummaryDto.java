package htw.webtech.financeMaster.rest.dto;

public class CategorySummaryDto {
    private Long categoryId;
    private String name;
    private double income;
    private double expense;

    public CategorySummaryDto() {}

    public CategorySummaryDto(Long categoryId, String name, double income, double expense) {
        this.categoryId = categoryId;
        this.name = name;
        this.income = income;
        this.expense = expense;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }

    public double getExpense() { return expense; }
    public void setExpense(double expense) { this.expense = expense; }
}
