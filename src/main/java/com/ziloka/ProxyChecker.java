package com.ziloka;

import com.ziloka.cmds.FindCommand;
import com.ziloka.services.Command;
import com.ziloka.services.CommandManagerService;

public class ProxyChecker {

    public static void main(String[] args) {

        CommandManagerService commandManagerService = new CommandManagerService();
        Command CommandFound = commandManagerService.get("find");
        CommandFound.run(args);
//        System.out.println(CommandFound.);

    }

}
