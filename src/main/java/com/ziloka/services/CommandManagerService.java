package com.ziloka.services;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManagerService {

    private final HashMap<String, Command> commands = new HashMap<String, Command>();

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

    public HashMap<String, ArrayList<String>> parseOptions(String[] args){
        // Example "--types http HTTPS --countries US --limit 10 --outfile ./proxies.txt"
        HashMap<String, ArrayList<String>> options = new HashMap<String, ArrayList<String>>();

        String lastOptionName = "";
        for(String argument: Arrays.copyOfRange(args, 1, args.length)){
            String lowerCaseArgument = argument.toLowerCase();
            if(lowerCaseArgument.matches("^-{1,2}\\w+")){
                Pattern optionNamePattern = Pattern.compile("(?!^-{1,2})\\w+");
                Matcher matcher = optionNamePattern.matcher(lowerCaseArgument);
                matcher.find();
                String optionName = matcher.group(0);
                lastOptionName = optionName;
            } else {
                ArrayList<String> currentValue = options.get(lastOptionName);
                ArrayList<String> arrayList = currentValue == null ? new ArrayList<String>() : currentValue;
                arrayList.add(argument);
                options.put(lastOptionName, arrayList);
            }
        }

        return options;
    }

}
