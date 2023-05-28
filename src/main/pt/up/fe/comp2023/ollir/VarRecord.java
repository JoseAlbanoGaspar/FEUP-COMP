package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public record VarRecord(Symbol var, int parNum, boolean isField, boolean isParameter, boolean isImported, boolean isInMethod) {

}
