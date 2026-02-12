package me.draskov.inputchecker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckElement {
    public String id = UUID.randomUUID().toString();
    public String name = "element";
    public List<String> tickInputs = new ArrayList<>();
    public List<Boolean> checkSprint = new ArrayList<>();
    public List<Boolean> checkJump = new ArrayList<>();
    public List<Boolean> checkSneak = new ArrayList<>();
    public List<Boolean> noSprint = new ArrayList<>();
    public List<Boolean> noJump = new ArrayList<>();
    public List<Boolean> noSneak = new ArrayList<>();

    public CheckElement() {
        initializeDefault();
    }

    public CheckElement(String name) {
        this.name = name;
        initializeDefault();
    }

    private void initializeDefault() {
        for (int i = 0; i < 100; i++) {
            tickInputs.add("");
            checkSprint.add(false);
            checkJump.add(false);
            checkSneak.add(false);
            noSprint.add(false);
            noJump.add(false);
            noSneak.add(false);
        }
    }
}

