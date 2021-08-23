package com.ziloka.services;

public abstract class Command {

    public Command() {

    }

    String name;
    String descr;
    String options;

    public abstract String getCommand();
    public abstract String getDescr();
    public abstract String getOptions();

    public abstract void run(String[] args);

}