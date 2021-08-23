package com.ziloka.services;

import com.ziloka.cmds.FindCommand;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

public class CommandManagerService {

    private HashMap<String, Command> commands = new HashMap<String, Command>();

    public CommandManagerService() {
        collectCommands();
    }

    public Command get(String parameter){
        return commands.get(parameter);
    }

    public void collectCommands() {

        // Disable org.reflection-reflection logging - very verbose
        // https://github.com/ronmamo/reflections/issues/266#issuecomment-878138509
        ch.qos.logback.classic.Logger root;
        root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.reflections");
        root.setLevel(ch.qos.logback.classic.Level.OFF);

        // https://github.com/ronmamo/reflections#usage

        // Find all classes located in the com.ziloka.cmds package
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setUrls(ClasspathHelper.forPackage("com.ziloka.cmds"));
        configurationBuilder.setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner());
        configurationBuilder.filterInputsBy(new FilterBuilder().includePackage("com.ziloka.cmds"));
        Reflections reflections = new Reflections(configurationBuilder);

        // Query scanner
        Set<Class<? extends Command>> commands = reflections.getSubTypesOf(Command.class);

        // Put all Commands in HashMap
        for(Class<? extends Command> cmd : commands){
            try {
                if (Modifier.isAbstract(cmd.getModifiers())) {
                    continue;
                }
                Command c = cmd.getConstructor().newInstance();
                this.commands.put(c.getCommand(), c);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                e.printStackTrace();
            }
        }
    }

}
