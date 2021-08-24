package com.ziloka.services;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Command {

    public Command() {

    }

    String name;
    String descr;
    String options;

    public abstract String getCommand();
    public abstract String getDescr();
    public abstract String getOptions();

    public abstract void run(HashMap<String, ArrayList<String>> options);

}