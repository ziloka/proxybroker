package com.ziloka;

import com.ziloka.cmds.FindCommand;
import com.ziloka.services.Command;
import com.ziloka.services.CommandManagerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class ProxyChecker {

    public static final Logger logger = LogManager.getLogger(FindCommand.class);

    public static void main(String[] args) {

        CommandManagerService commandManagerService = new CommandManagerService();
        Command CommandFound = commandManagerService.get(args[0]);
        HashMap<String, ArrayList<String>> options = commandManagerService.parseOptions(args);
        CommandFound.run(options);

    }

}
