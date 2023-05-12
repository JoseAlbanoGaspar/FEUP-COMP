package pt.up.fe.comp2023;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        // Analysis stage
        Analysis analysis = new Analysis();
        JmmSemanticsResult semanticsResult = analysis.semanticAnalysis(parserResult);
        TestUtils.noErrors(semanticsResult.getReports());

        //ollir
        OllirParser ollir = new OllirParser();
        OllirResult ollirResult = ollir.toOllir(semanticsResult);
        System.out.println(ollirResult.getOllirCode());

        //jasmin
        Backend jasmin = new Backend();
        JasminResult jasminResult = jasmin.toJasmin(ollirResult);

        System.out.println(jasminResult.getJasminCode());

        // ... add remaining stages
        TestUtils.runJasmin(jasminResult.getJasminCode());
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Create config
        Map<String, String> config = new HashMap<>();

        int r = -1;
        boolean oFlag = false;
        boolean dFlag = false;
        String inputFile = null;

        for (String arg : args) {
            if (arg.startsWith("-r="))
                r = Integer.parseInt(arg.substring(3));
            else if (arg.equals("-o"))
                oFlag = true;
            else if (arg.equals("-d"))
                dFlag = true;
            else if (arg.startsWith("-i="))
                inputFile = arg.substring(3);
        }

        if (inputFile == null) {
            throw new RuntimeException("Usage: jmm [-r=<num>] [-o] [-d] -i=<input file.jmm>");
        }

        config.put("inputFile", inputFile);
        config.put("optimize", String.valueOf(oFlag));
        config.put("registerAllocation", String.valueOf(r));
        config.put("debug", String.valueOf(dFlag));

        return config;
    }

}
