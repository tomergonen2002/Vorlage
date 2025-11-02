package htw.webtech.financeMaster.rest.dto;

import java.util.List;

public class StatsDto {
    private int totalTransactions;
    private double totalIncome;
    private double totalExpense;
    private double net;
    private List<CategorySummaryDto> byCategory;

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public double getNet() { return net; }
    public void setNet(double net) { this.net = net; }

    public List<CategorySummaryDto> getByCategory() { return byCategory; }
    public void setByCategory(List<CategorySummaryDto> byCategory) { this.byCategory = byCategory; }
}
