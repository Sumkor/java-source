package com.sumkor.formula;

import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sumkor
 * @since 2022/2/21
 */
public class SpELTest {

	/**
	 * Spring EL 表达式计算公式
	 */
	@Test
	public void exec() {
		String spelExpression = "#diffDead/#average * #score";
		Map<String, Object> variables = new HashMap<>();
		variables.put("diffDead", 25);
		variables.put("average", 21.0);
		variables.put("score", 100);

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setVariables(variables);
		Expression exp = parser.parseExpression(spelExpression);
		Integer value = exp.getValue(context, Integer.class);
		System.out.println("value：" + value);
	}

	@Test
	public void exec02() {
		String str = "(1+2)/3";
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(str);
		BigDecimal bigDecimal = exp.getValue(BigDecimal.class);
		System.out.println("bigDecimal = " + bigDecimal);
	}
}
