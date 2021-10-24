package main

import (
	"embed"
	"fmt"
	"log"
	"os"
	"runtime"
	"github.com/Ziloka/ProxyBroker/cmds"
	"github.com/urfave/cli/v2"
)

// https://stackoverflow.com/questions/66285635/how-do-you-use-go-1-16-embed-features-in-subfolders-packages
// https://stackoverflow.com/a/67357103

//go:embed assets/*
var assetFS embed.FS

func main() {

	runtime.GOMAXPROCS(runtime.NumCPU())

	app := &cli.App{
		Name:  "ProxyBroker",
		Usage: "proxybroker find",
		UseShortOptionHandling: true,
		Flags: []cli.Flag{
			&cli.BoolFlag{Name: "verbose", Aliases: []string{"v"}},
		},
		Commands: []*cli.Command{
			{
				Name: "find",
				Aliases: []string{"f"},
				Usage: "Find and check proxies",
				Flags: []cli.Flag{
					&cli.StringFlag{Name: "types", Aliases: []string{"t"}},
					&cli.StringFlag{Name: "timeout", Aliases: []string{"to"}},
					&cli.StringFlag{Name: "countries", Aliases: []string{"c"}},
					&cli.StringFlag{Name: "ports", Aliases: []string{"p"}},
					&cli.StringFlag{Name: "lvl", Aliases: []string{"l"}},
				},
				Action: func(c *cli.Context) error {
					// Run cmd using go run main.go find"
					err := cmds.Find(c, assetFS)
					return err
				},
			},
			{
				Name: "grab",
				Aliases: []string{"g"},
				Usage: "Grab proxies from sites",
				Flags: []cli.Flag{
					&cli.StringFlag{Name: "types", Aliases: []string{"t"}},
					&cli.StringFlag{Name: "timeout", Aliases: []string{"to"}},
					&cli.StringFlag{Name: "countries", Aliases: []string{"c"}},
					&cli.StringFlag{Name: "ports", Aliases: []string{"p"}},
					&cli.StringFlag{Name: "lvl", Aliases: []string{"l"}},
				},
				Action: func (c *cli.Context) error {
					// Run cmd using go run main.go grab"
					err := cmds.Grab(c, assetFS)
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
