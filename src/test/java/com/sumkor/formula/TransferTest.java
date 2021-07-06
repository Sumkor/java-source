package com.sumkor.formula;

import org.junit.Test;

import java.util.Stack;

/**
 * 将公式转换为后缀表达式，再进行运算
 * https://blog.csdn.net/qq_27512671/article/details/82982362
 * 本例只支持个位数，不支持小数点和多位数
 *
 * @author Sumkor
 * @since 2021/7/6
 */
public class TransferTest {

    @Test
    public void transferTest() {
//        String str = "1+2*(3-4*(5+6))"; // 123456+*-*+
//        String str = "(1-2)*3+4"; // 12-3*4+
        String str = "5-1*(5+6)+2"; // 5156+*2+-
        System.out.println(transfer(str));
    }

    /**
     * 中缀表达式转后缀表达式
     * 例如 a+b*(c-d) 转为后缀表达式就是 abcd-*+
     *
     * 后缀表达式实际上是将原始表达式划分为【数字】和【运算符】两部分
     * 其中，【数字】以原始表达式从左到右的方式排序，【运算符】以优先级从左到右排序
     */
    public static String transfer(String mathStr) {
        // 标记输出结果
        StringBuilder result = new StringBuilder();
        // 1.初始化一个运算符栈。
        Stack<Character> stack = new Stack<>();
        if (mathStr == null || mathStr.length() == 0) {
            return null;
        }
        System.out.println("--------------");
        System.out.println("中缀表达式：" + mathStr);
        char[] arr = mathStr.toCharArray();
        // 2.从算数表达式输入的字符串中依次从左向右每次读取一个字符。
        for (char s : arr) {
            // 3.如果当前字符是数字（操作数），则直接填写到后缀表达式。
            if (Character.isDigit(s)) {
                result.append(s);
            }
            // 4.如果当前字符是（左括号，将其压入运算符栈。
            else if ('(' == s) {
                stack.push(s);
            }
            // 5.如果当前字符为运算符，则
            else if ('+' == s || '-' == s || '*' == s || '/' == s) {
                if (!stack.isEmpty()) {
                    char stackTop = stack.pop();
                    // 当此运算符的优先级高于栈顶元素的时候，则将此运算符压入运算符栈
                    if (compare(s, stackTop)) {
                        stack.push(stackTop);
                    }
                    // 否则，弹出栈顶运算符到后缀表达式，并且将当前运算符压栈
                    else {
                        result.append(stackTop);
                    }
                }
                stack.push(s);
            }
            // 6.如果当前字符是）右括号，反复将栈顶元素弹出到后缀表达式，直到栈顶元素是左括号（为止，并将左括号从栈中弹出丢弃。
            else if (s == ')') {
                while (!stack.isEmpty()) {
                    char item = stack.pop();
                    if (item != '(') {
                        result.append(item);
                    } else {
                        break;
                    }
                }
            }
        }
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }
        System.out.println("后缀表达式：" + result.toString());
        return result.toString();
    }

    /**
     * 比较优先级
     * 返回 true 表示 curr 优先级大于 stackTop
     */
    private static boolean compare(char curr, char stackTop) {
        if (stackTop == '(') { // 左括号会直接入栈，这里是其他运算符与栈顶左括号对比
            return true;
        }
        if (curr == '*' || curr == '/') {
            return stackTop == '+' || stackTop == '-';
        }
        return false;
    }

    @Test
    public void testCompare() {
        System.out.println(compare('*', '/')); // false
        System.out.println(compare('*', '+')); // true
        System.out.println(compare('*', '(')); // true
        System.out.println(compare('*', ')')); // false
    }

    /**
     * 根据后缀表达式，计算结果
     */
    public static int calculate(String transferToPostfix) {
        Stack<Integer> stack = new Stack<>();
        char[] c = transferToPostfix.toCharArray();
        int a, b;
        for (char value : c) {
            switch (value) {
                case '+':
                    a = Integer.parseInt(stack.pop().toString());
                    b = Integer.parseInt(stack.pop().toString());
                    stack.push(b + a);
                    break;
                case '-':
                    a = Integer.parseInt(stack.pop().toString());
                    b = Integer.parseInt(stack.pop().toString());
                    stack.push(b - a);
                    break;
                case '*':
                    a = Integer.parseInt(stack.pop().toString());
                    b = Integer.parseInt(stack.pop().toString());
                    stack.push(b * a);
                    break;
                case '/':
                    a = Integer.parseInt(stack.pop().toString());
                    b = Integer.parseInt(stack.pop().toString());
                    stack.push(b / a);
                    break;
                default:
                    stack.push(Integer.valueOf(Character.toString(value)));
                    break;
            }
        }
        return stack.pop();
    }

    @Test
    public void calculateTest() {
        String str = "5-1*(5+6)+2";
        String transfer = transfer(str);
        System.out.println(transfer);
        assert transfer != null;
        System.out.println(calculate(transfer));
    }
}
