package pt.up.fe.comp2023.backend;

import org.specs.comp.ollir.Descriptor;

import java.util.HashMap;
import java.util.Map;

public class RegisterHandler {
    private final Map<String, Descriptor> varTable;
    private final Map<Integer, Integer> localVars;
    private Integer nLocalVars;

    public RegisterHandler(Map<String, Descriptor> varTable, boolean isStatic) {
        this.varTable = varTable;
        this.localVars = new HashMap<>();
        nLocalVars = isStatic ? 0 : 1;
    }

    public void loadVariable(String varName) {
        int virtualReg = varTable.get(varName).getVirtualReg();

        // variable is already loaded
        if (localVars.containsKey(virtualReg)) return;

        localVars.put(virtualReg, nLocalVars);
        nLocalVars++;
    }

    public int getRegisterOf(String varName) {
        int virtualReg = varTable.get(varName).getVirtualReg();

        if (localVars.containsKey(virtualReg)) return localVars.get(virtualReg);

        loadVariable(varName);
        return localVars.get(virtualReg);
    }

    public int getLocalLimits() { return this.nLocalVars; }
}
