package com.sumkor.formula;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Stack;

/**
 * 数学公式计算
 *
 * @author Sumkor
 * @since 2021/7/6
 */
public class FormulaTest {

    @Test
    public void scriptEngine() throws ScriptException {
        String str = "43*(2+1.4)+2*32/(3-2.1)";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = engine.eval(str);
        System.out.println("结果类型:" + result.getClass().getName() + ",计算结果:" + result);
    }


}
