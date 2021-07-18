package moe.ore;

import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.Set;

public class TarsClassFieIdKV {

    public String className;
    public Set<Element> fieIds = new HashSet<>();

    public TarsClassFieIdKV(String className) {
        this.className = className;
    }
}
