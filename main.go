package main

import (
	"fmt"
	"log"
	"os"
	"github.com/urfave/cli/v2"
	"github.com/Ziloka/ProxyBroker/services"
)

func main() {
	app := &cli.App{
		Name: "ProxyBroker",
		Usage: "proxybroker find",
		Flags: []cli.Flag {
			&cli.StringFlag{
				Name: "find",
				Value: "http",
				Usage: "proxy protocol",
			},
		},
		Action: func(c *cli.Context) error {

			proxies := services.Collect()
			for _, proxy := range proxies {
				go services.Check(proxy)
			}
			fmt.Println("Hello World!")
			return nil
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}