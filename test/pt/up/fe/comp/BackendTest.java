package pt.up.fe.comp;
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.HashMap;

public class BackendTest {

    // @Test
    // public void testHelloWorld() {
    // var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
    // TestUtils.noErrors(result.getReports());
    // var output = result.run();
    // assertEquals("Hello, World!", output.trim());
    // }

    @Test
    public void testHelloWorld() {

        String jasminCode = SpecsIo.getResource("pt/up/fe/comp/jasmin/HelloWorld.j");
        var output = TestUtils.runJasmin(jasminCode);
        assertEquals("Hello World!\nHello World Again!\n", SpecsStrings.normalizeFileContents(output));
    }

    @Test
    public void testArithmetics() {

        String jmmCode = SpecsIo.getResource("pt/up/fe/comp/cp2/jasmin/OllirToJasminArithmetics.jmm");
        var jasminResult = TestUtils.backend(jmmCode);
        var output = TestUtils.runJasmin(jasminResult.getJasminCode());

        assertEquals("3", output);
    }

    @Test
    public void testBasic() {

        String jmmCode = SpecsIo.getResource("pt/up/fe/comp/cp2/jasmin/OllirToJasminBasic.jmm");
        var jasminResult = TestUtils.backend(jmmCode);
        var output = TestUtils.runJasmin(jasminResult.getJasminCode());

        assertEquals("", output);
    }

    @Test
    public void testInvoke() {

        String jmmCode = SpecsIo.getResource("pt/up/fe/comp/cp2/jasmin/OllirToJasminInvoke.jmm");
        var jasminResult = TestUtils.backend(jmmCode);
        var output = TestUtils.runJasmin(jasminResult.getJasminCode());

        assertEquals("", output);
    }
}
