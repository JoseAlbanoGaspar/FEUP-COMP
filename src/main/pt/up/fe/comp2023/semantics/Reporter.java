package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public interface Reporter {
    List<Report> getReports();

    Void visit(JmmNode node);


}
