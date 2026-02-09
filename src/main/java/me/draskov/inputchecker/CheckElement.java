package me.draskov.inputchecker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckElement {
    public String id = UUID.randomUUID().toString();
    public String name = "element";

    // index 0 = tick 1, index 1 = tick 2, ...
    public List<String> tickInputs = new ArrayList<String>();

    public CheckElement() {}
    public CheckElement(String name) {
        this.name = name;
    }
}
