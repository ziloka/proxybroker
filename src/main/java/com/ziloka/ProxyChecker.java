package com.ziloka;

import com.ziloka.cmds.FindCommand;
import com.ziloka.services.Command;
import com.ziloka.services.CommandManagerService;

public class ProxyChecker {

    public static void main(String[] args) {

        CommandManagerService commandManagerService = new CommandManagerService();
        Command CommandFound = commandManagerService.get(args[0]);
        CommandFound.run(args);
        System.out.println(commandManagerService.get("test"));

    }

}
