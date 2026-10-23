package ccrrs.algorithms.decision;

import ccrrs.model.Complaint;
import java.util.ArrayList;
import java.util.List;

public class KnapsackBudgetOptimizer {

    public static class BudgetPlanResult {
        public final List<Complaint> fundedCases;
        public final int totalCost;
        public final int totalResolutionValue;
        public final int budgetLimit;
        public final int remainingBudget;
        public final int[][] dpTable;

        public BudgetPlanResult(List<Complaint> fundedCases, int totalCost,
                                int totalResolutionValue, int budgetLimit, int[][] dpTable) {
            this.fundedCases = fundedCases;
            this.totalCost = totalCost;
            this.totalResolutionValue = totalResolutionValue;
            this.budgetLimit = budgetLimit;
            this.remainingBudget = budgetLimit - totalCost;
            this.dpTable = dpTable;
        }

        public void printPlan() {
            System.out.println("==========================================================================================");
            System.out.println("         DYNAMIC PROGRAMMING 0/1 KNAPSACK: OPTIMAL FORENSIC BUDGET ALLOCATION             ");
            System.out.println("==========================================================================================");
            System.out.printf("Forensic Budget Available: Rs. %,d,000%n", budgetLimit);
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("%-12s | %-18s | %-20s | %-14s | %s%n",
                    "Case ID", "Complainant", "Category", "Forensic Cost", "Resolution Value");
            System.out.println("------------------------------------------------------------------------------------------");
            for (Complaint c : fundedCases) {
                System.out.printf("%-12s | %-18s | %-20s | Rs. %2d,000      | %d pts%n",
                        c.getCaseId(), c.getComplainantName(), c.getCategory(),
                        c.getInvestigationCost(), c.getExpectedResolutionValue());
            }
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("Total Forensic Expenditure : Rs. %,d,000%n", totalCost);
            System.out.printf("Remaining Unallocated Funds: Rs. %,d,000%n", remainingBudget);
            System.out.printf("Maximum Total Resolution Payoff: %d points%n", totalResolutionValue);
            System.out.println("==========================================================================================");
        }
    }

    /**
     * Solves the 0/1 Knapsack problem for investigation budget allocation.
     *
     * @param cases List of candidate complaints requiring forensic funding.
     * @param budgetLimit Total monthly budget in ₹ thousands.
     */
    public static BudgetPlanResult optimizeBudget(List<Complaint> cases, int budgetLimit) {
        if (cases == null || cases.isEmpty() || budgetLimit <= 0) {
            return new BudgetPlanResult(new ArrayList<>(), 0, 0, budgetLimit, new int[0][0]);
        }

        int n = cases.size();
        int[][] dp = new int[n + 1][budgetLimit + 1];

        // Fill DP table
        for (int i = 1; i <= n; i++) {
            Complaint c = cases.get(i - 1);
            int cost = c.getInvestigationCost();
            int value = c.getExpectedResolutionValue();

            for (int w = 0; w <= budgetLimit; w++) {
                if (cost <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], value + dp[i - 1][w - cost]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Trace back to recover the optimal subset of funded cases
        List<Complaint> fundedCases = new ArrayList<>();
        int w = budgetLimit;
        int totalCost = 0;

        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Complaint selected = cases.get(i - 1);
                fundedCases.add(selected);
                totalCost += selected.getInvestigationCost();
                w -= selected.getInvestigationCost();
            }
        }

        return new BudgetPlanResult(fundedCases, totalCost, dp[n][budgetLimit], budgetLimit, dp);
    }
}
