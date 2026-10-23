package ccrrs.ds.linear;

/**
 * Module 2: Priority Score Evaluator using Stack.
 * Implements:
 * 1. Shunting-Yard Infix to Postfix conversion using Stack.
 * 2. Postfix expression evaluation using Stack.
 * 3. Priority calculation formula: (severity * 3) + (loss_norm * 4) + (victims * 2)
 */
public class PostfixPriorityEvaluator {

    /**
     * Computes the priority score for a complaint using postfix evaluation.
     * Normalized Financial Loss = min(financialLoss / 10000.0, 50.0)
     * Formula: (severity * 3) + (normalizedLoss * 4) + (victims * 2)
     */
    public static double computeCasePriority(int severity, double financialLoss, int victimCount) {
        double normalizedLoss = Math.min(financialLoss / 10000.0, 50.0);

        // Build postfix tokens representing:
        // severity 3 * normalizedLoss 4 * + victimCount 2 * +
        String[] postfixTokens = {
                String.valueOf(severity), "3", "*",
                String.valueOf(normalizedLoss), "4", "*", "+",
                String.valueOf(victimCount), "2", "*", "+"
        };

        return evaluatePostfixTokens(postfixTokens);
    }

    /**
     * Evaluates an array of postfix tokens using LinkedStack.
     * Time Complexity: O(n)
     */
    public static double evaluatePostfixTokens(String[] tokens) {
        LinkedStack<Double> stack = new LinkedStack<>();

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid postfix expression: insufficient operands for " + token);
                }
                double op2 = stack.pop();
                double op1 = stack.pop();
                double result = applyOperator(token.charAt(0), op1, op2);
                stack.push(result);
            } else {
                stack.push(Double.parseDouble(token));
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix expression: remaining operands on stack.");
        }
        return stack.pop();
    }

    /**
     * Converts standard infix expression string to postfix using Shunting-yard algorithm.
     * Supported operators: +, -, *, /, (, )
     */
    public static String infixToPostfix(String infix) {
        StringBuilder postfix = new StringBuilder();
        LinkedStack<Character> stack = new LinkedStack<>();

        for (int i = 0; i < infix.length(); i++) {
            char ch = infix.charAt(i);

            if (Character.isWhitespace(ch)) continue;

            if (Character.isLetterOrDigit(ch) || ch == '.') {
                // Read complete operand
                while (i < infix.length() && (Character.isLetterOrDigit(infix.charAt(i)) || infix.charAt(i) == '.')) {
                    postfix.append(infix.charAt(i));
                    i++;
                }
                postfix.append(" ");
                i--; // Backtrack because loop will increment
            } else if (ch == '(') {
                stack.push(ch);
            } else if (ch == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop()).append(" ");
                }
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop(); // Remove '('
                }
            } else if (isOperator(String.valueOf(ch))) {
                while (!stack.isEmpty() && precedence(ch) <= precedence(stack.peek())) {
                    postfix.append(stack.pop()).append(" ");
                }
                stack.push(ch);
            }
        }

        while (!stack.isEmpty()) {
            postfix.append(stack.pop()).append(" ");
        }

        return postfix.toString().trim();
    }

    private static boolean isOperator(String s) {
        return s.length() == 1 && "+-*/".indexOf(s.charAt(0)) != -1;
    }

    private static int precedence(char ch) {
        switch (ch) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    private static double applyOperator(char op, double a, double b) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero in priority evaluation.");
                return a / b;
            default:
                throw new IllegalArgumentException("Unknown operator: " + op);
        }
    }
}
