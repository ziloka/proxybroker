package main

import (
	"fmt"
	"log"
	"os"
	"runtime"
	"github.com/Ziloka/ProxyBroker/cmds"
	"github.com/urfave/cli/v2"
)

func main() {

	runtime.GOMAXPROCS(runtime.NumCPU())

	app := &cli.App{
		Name:  "ProxyBroker",
		Usage: "proxybroker find",
		Flags: []cli.Flag{
			&cli.StringFlag{
				Name:  "find",
				Value: "http",
				Usage: "proxy protocol",
			},
		},
		Commands: []*cli.Command{
			{
				Name: "find",
				Aliases: []string{"f"},
				Usage: "Find and check proxies",
				Action: func(c *cli.Context) error {
					// Run cmd using go run main.go find"
					err := cmds.Find(c)
					return err
				},
			},
			{
				Name: "grab",
				Aliases: []string{"g"},
				Usage: "Grab proxies from sites",
				Action: func (c *cli.Context) error {
					// Run cmd using go run main.go grab"
					err := cmds.Grab(c)
					return err
				},
			},
		},
		
		Action: func(c *cli.Context) error {
			
			fmt.Println("ProxyBroker find")
			return nil
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
