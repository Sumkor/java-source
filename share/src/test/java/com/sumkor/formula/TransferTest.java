package com.sumkor.formula;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Stack;

/**
 * 数学公式计算，支持多位数、小数点
 *
 * @author Sumkor
 * @since 2021/7/6
 */
public class TransferTest {

    @Test
    public void calculateTest() {
        String str = "5-1*(5+6)+2";
        System.out.println(str + " = " + calculate(str));

        str = "50-1*(5+6)+2";
        System.out.println(str + " = " + calculate(str));

        str = "(50.5-1)*(5+6)+2";
        System.out.println(str + " = " + calculate(str));

        str = "1+2*(3-4*(5+6))";
        System.out.println(str + " = " + calculate(str));

        str = "1/2*(3-4*(5+6))*10";
        System.out.println(str + " = " + calculate(str));

        str = "43*(2+1)+2*32+98";
        System.out.println(str + " = " + calculate(str));

        str = "3-10*2+5";
        System.out.println(str + " = " + calculate(str));
    }

    /**
     * 1. 将中缀表达式转后缀表达式，例如 a+b*(c-d) 转为后缀表达式就是 abcd-*+
     * 2. 根据后缀表达式进行计算
     */
    public static BigDecimal calculate(String mathStr) {
        if (mathStr == null || mathStr.length() == 0) {
            return null;
        }
        LinkedList<String> postfixList = getPostfix(mathStr);
        // System.out.println("后缀表达式：" + postfixList.toString());
        return doCalculate(postfixList);
    }

    /**
     * 将中缀表达式，转换为后缀表达式，支持多位数、小数
     */
    private static LinkedList<String> getPostfix(String mathStr) {
        // 后缀表达式链（FIFO）
        LinkedList<String> postfixList = new LinkedList<>();
        // 运算符栈（FILO）
        Stack<Character> optStack = new Stack<>();
        // 多位数链（FIFO）
        LinkedList<Character> multiDigitList = new LinkedList<>();
        char[] arr = mathStr.toCharArray();
        for (char c : arr) {
            if (Character.isDigit(c) || '.' == c) {
                multiDigitList.addLast(c);
            } else {
                // 处理当前的运算符之前，先处理多位数链中暂存的数据
                if (!multiDigitList.isEmpty()) {
                    StringBuilder temp = new StringBuilder();
                    while (!multiDigitList.isEmpty()) {
                        temp.append(multiDigitList.removeFirst());
                    }
                    postfixList.addLast(temp.toString());
                }
            }
            // 如果当前字符是左括号，将其压入运算符栈
            if ('(' == c) {
                optStack.push(c);
            }
            // 如果当前字符为运算符
            else if ('+' == c || '-' == c || '*' == c || '/' == c) {
                while (!optStack.isEmpty()) {
                    char stackTop = optStack.pop();
                    // 若当前运算符的优先级高于栈顶元素，则一起入栈
                    if (compare(c, stackTop)) {
                        optStack.push(stackTop);
                        break;
                    }
                    // 否则，弹出栈顶运算符到后缀表达式，继续下一次循环
                    else {
                        postfixList.addLast(String.valueOf(stackTop));
                    }
                }
                optStack.push(c);
            }
            // 如果当前字符是右括号，反复将运算符栈顶元素弹出到后缀表达式，直到栈顶元素是左括号为止，并将左括号从栈中弹出丢弃。
            else if (c == ')') {
                while (!optStack.isEmpty()) {
                    char stackTop = optStack.pop();
                    if (stackTop != '(') {
                        postfixList.addLast(String.valueOf(stackTop));
                    } else {
                        break;
                    }
                }
            }
        }
        // 遍历结束时，若多位数链中具有数据，说明公式是以数字结尾
        if (!multiDigitList.isEmpty()) {
            StringBuilder temp = new StringBuilder();
            while (!multiDigitList.isEmpty()) {
                temp.append(multiDigitList.removeFirst());
            }
            postfixList.addLast(temp.toString());
        }
        // 遍历结束时，运算符栈若有数据，说明是由括号所致，需要补回去
        while (!optStack.isEmpty()) {
            postfixList.addLast(String.valueOf(optStack.pop()));
        }
        return postfixList;
    }

    /**
     * 根据后缀表达式，得到计算结果，保留两位小数
     */
    private static BigDecimal doCalculate(LinkedList<String> postfixList) {
        // 操作数栈
        Stack<BigDecimal> numStack = new Stack<>();
        while (!postfixList.isEmpty()) {
            String item = postfixList.removeFirst();
            BigDecimal a, b;
            switch (item) {
                case "+":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.add(a));
                    break;
                case "-":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.subtract(a));
                    break;
                case "*":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.multiply(a));
                    break;
                case "/":
                    a = numStack.pop();
                    b = numStack.pop();
                    numStack.push(b.divide(a, 2, RoundingMode.HALF_UP));
                    break;
                default:
                    numStack.push(new BigDecimal(item));
                    break;
            }
        }
        BigDecimal result = numStack.pop();
        if (result != null) {
            result = result.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        }
        return result;
    }

    /**
     * 比较优先级
     * 返回 true 表示 curr 优先级大于 stackTop
     */
    private static boolean compare(char curr, char stackTop) {
        // 左括号会直接入栈，这里是其他运算符与栈顶左括号对比
        if (stackTop == '(') {
            return true;
        }
        // 乘除法的优先级大于加减法
        if (curr == '*' || curr == '/') {
            return stackTop == '+' || stackTop == '-';
        }
        // 运算符优先级相同时，先入栈的优先级更高
        return false;
    }

    @Test
    public void stackToString() {
        Stack<String> stack = new Stack<>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        System.out.println(stack.toString());
        stack.clear();
        System.out.println(stack.toString());
    }
}
