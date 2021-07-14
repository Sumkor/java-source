package com.sumkor.formula;

import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;

/**
 * 数学公式计算
 *
 * @author Sumkor
 * @since 2021/7/6
 */
public class FormulaTest {

    /**
     * 使用内置JS引擎
     */
    @Test
    public void scriptEngine() throws ScriptException {
        String str = "43*(2+1.4)+2*32/(3-2.1)";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = engine.eval(str);
        System.out.println("结果类型:" + result.getClass().getName() + ",计算结果:" + result);
    }

    /**
     * 耗时对比
     */
    @Test
    public void vs() throws ScriptException {
        long start = System.currentTimeMillis();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        for (int i = 0; i < 1000; i++) {
            String str = "43*(2+1.4)+2*32/(3-2.1)" + "+" + i;
            Object result = engine.eval(str);
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            String str = "43*(2+1.4)+2*32/(3-2.1)" + "+" + i;
            BigDecimal result = TransferTest.calculate(str);
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
    }

    /**
     * 结果对比
     */
    @Test
    public void equal() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        for (int i = 0; i < 100; i++) {
            String str = "43*(2+1)+2*32" + "+" + i;
            Object result0 = engine.eval(str);
            BigDecimal result1 = TransferTest.calculate(str);
            System.out.println( result0 + " " + result1 + " " + i);
            Assert.assertEquals(0, BigDecimal.valueOf((Integer) result0).compareTo(result1));
        }
    }
}
