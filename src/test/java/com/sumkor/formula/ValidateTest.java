package com.sumkor.formula;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Sumkor
 * @since 2021/7/14
 */
public class ValidateTest {

    @Test
    public void test() {
        List<String> variables = Arrays.asList("height", "length", "width", "num");

        String result01 = validate("(height+length)(width+num)", variables);
        System.out.println("result01 = " + result01);

        String result02 = validate("-num+100", variables);
        System.out.println("result02 = " + result02);

        String result03 = validate("(length*(1+width)/height)*num", variables);
        System.out.println("result03 = " + result03);
    }

    /**
     * 使用正则来校验数学公式
     *
     * @param expression 数学公式，包含变量
     * @param variables  内置变量集合
     */
    private String validate(String expression, List<String> variables) {
        if (variables == null || variables.isEmpty()) {
            throw new RuntimeException("内置变量为空");
        }
        // 去空格
        expression = expression.replaceAll(" ", "");
        // 连续运算符处理
        if (expression.split("[\\+\\-\\*\\/]{2,}").length > 1) {
            throw new RuntimeException("公式不合法，包含连续运算符");
        }
        if (StringUtils.contains(expression, "()")) {
            throw new RuntimeException("公式不合法，包含空括号");
        }
        expression = expression.replaceAll("\\)\\(", "\\)*\\(");
        expression = expression.replaceAll("\\(\\-", "\\(0-");
        expression = expression.replaceAll("\\(\\+", "\\(0+");
        // 校验变量
        String[] splits = expression.split("\\+|\\-|\\*|\\/|\\(|\\)");
        for (String split : splits) {
            if (StringUtils.isBlank(split) || Pattern.matches("[0-9]+", split)) {
                continue;
            }
            if (!variables.contains(split)) {
                throw new RuntimeException("公式不合法，包含非法变量或字符");
            }
        }
        // 校验括号
        Character preChar = null;
        Stack<Character> stack = new Stack<>();
        String resultExpression = expression;
        for (int i = 0; i < expression.length(); i++) {
            char currChar = expression.charAt(i);
            if (i == 0) {
                if (Pattern.matches("\\*|\\/", String.valueOf(currChar))) {
                    throw new RuntimeException("公式不合法，以错误运算符开头");
                }
                if (currChar == '+') {
                    resultExpression = expression.substring(1);
                }
                if (currChar == '-') {
                    resultExpression = "0" + expression;
                }
            }
            if ('(' == currChar) {
                stack.push('(');
            } else if (')' == currChar) {
                if (stack.size() > 0) {
                    stack.pop();
                } else {
                    throw new RuntimeException("公式不合法，括号不配对");
                }
            }
            if (preChar != null && preChar == '(' && Pattern.matches("[\\+\\-\\*\\/]+", String.valueOf(currChar))) {
                throw new RuntimeException("公式不合法，左括号后是运算符");
            }
            if (preChar != null && preChar == ')' && !Pattern.matches("[\\+\\-\\*\\/]+", String.valueOf(currChar))) {
                throw new RuntimeException("公式不合法，右括号后面不是运算符");
            }
            if (i == expression.length() - 1) {
                if (Pattern.matches("\\+|\\-|\\*|\\/", String.valueOf(currChar)))
                    throw new RuntimeException("公式不合法，以运算符结尾");
            }
            preChar = currChar;
        }
        if (stack.size() > 0) {
            throw new RuntimeException("公式不合法，括号不配对");
        }
        return resultExpression;
    }
}
