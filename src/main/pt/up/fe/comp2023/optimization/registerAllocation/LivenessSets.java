package pt.up.fe.comp2023.optimization.registerAllocation;

import java.util.HashSet;
import java.util.Set;

public class LivenessSets {
    private Set<String> in;
    private Set<String> out;
    private Set<String> use;
    private Set<String> def;

    public LivenessSets() {
        in = new HashSet<>();
        out = new HashSet<>();
        use = new HashSet<>();
        def = new HashSet<>();
    }

    public Set<String> getIn() {
        return in;
    }

    public Set<String> getOut() {
        return out;
    }

    public Set<String> getDef() {
        return def;
    }

    public Set<String> getUse() {
        return use;
    }

    public void setIn(Set<String> union) {
        in = union;
    }
}
