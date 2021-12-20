///usr/bin/true; exec /usr/bin/env go run "$0" "$@"
// https://stackoverflow.com/questions/7707178/whats-the-appropriate-go-shebang-line

package main

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/cmds"
	"github.com/urfave/cli/v2"
	"log"
	"os"
	"time"
	"runtime"
)

var (
	SHA_HASH      string // sha1 revision used to build the program
	BUILD_TIME    string // when the executable was built
	BUILD_VERSION string // program version
)

// https://stackoverflow.com/questions/66285635/how-do-you-use-go-1-16-embed-features-in-subfolders-packages
// https://stackoverflow.com/a/67357103

//go:embed assets/*
var assetFS embed.FS

func main() {

	runtime.GOMAXPROCS(runtime.NumCPU())

	app := &cli.App{
		Name:                   "ProxyBroker",
		Usage:                  "proxybroker find",
		UseShortOptionHandling: true,
		Commands: []*cli.Command{
			{
				Name:    "version",
				Aliases: []string{"v"},
				Usage:   "displays build information",
				Action: func(c *cli.Context) error {
					fmt.Printf("Build on %s from sha1 %s\nBuild Version: %s\nCompiled Date: %s\n", BUILD_TIME, SHA_HASH, BUILD_VERSION, c.App.Compiled);
					return nil
				},
			},
			{
				Name:    "find",
				Aliases: []string{"f"},
				Usage:   "Find and check proxies",
				Flags: []cli.Flag{
					&cli.BoolFlag{
						Name: "raw",
						Aliases: []string{"r"},
						Value: false,
						DefaultText: "false",
					},
					&cli.StringSliceFlag{
						Name: "types",
						Aliases: []string{"t"},
						Value: cli.NewStringSlice("http", "https"),
						DefaultText: "http, https",
					},
					&cli.DurationFlag{
						Name: "timeout",
						Aliases: []string{"to"},
						Value: 5000,
						DefaultText: "5000",
					},
					&cli.StringSliceFlag{
						Name: "countries",
						Aliases: []string{"c"},
					},
					&cli.IntSliceFlag{
						Name: "ports",
						Aliases: []string{"p"},
					},
					&cli.StringFlag{
						Name: "lvl",
						Aliases: []string{"l"},
					},
					&cli.IntFlag{
						Name: "limit",
						Value: 10,
						DefaultText: "10",
					},
				},
				Action: func(c *cli.Context) error {
					// Run cmd using go run main.go find"
					err := cmds.Find(c, assetFS)
					return err
				},
			},
			{
				Name:    "check",
				Aliases: []string{"c"},
				Usage:   "checks given proxies in file",
				Flags: []cli.Flag{
					&cli.BoolFlag{
						Name: "raw",
						Aliases: []string{"r"},
						Value: false,
						DefaultText: "false",
					},
					&cli.StringSliceFlag{
						Name: "types",
						Aliases: []string{"t"},
						Value: cli.NewStringSlice("http", "https"),
						DefaultText: "http, https",
					},
					&cli.DurationFlag{
						Name: "timeout",
						Aliases: []string{"tmo"},
						Value: time.Second * 5,
						DefaultText: "5000",
					},
					&cli.StringSliceFlag{
						Name: "countries",
						Aliases: []string{"c"},
					},
					&cli.IntSliceFlag{
						Name: "ports",
						Aliases: []string{"p"},
					},
					&cli.StringFlag{
						Name: "lvl",
						Aliases: []string{"l"},
						Value: "High",
						DefaultText: "High",
					},
					&cli.StringFlag{
						Name: "input",
						Aliases: []string{"i", "file"},
						Value: "proxies.txt",
						DefaultText: "proxies.txt",
					},
				},
				Action: func(c *cli.Context) error {
					err := cmds.Check(c, assetFS)
					return err
				},
			},
			{
				Name:    "grab",
				Aliases: []string{"g"},
				Usage:   "Grab proxies from sites",
				Flags: []cli.Flag{
					&cli.StringFlag{
						Name: "outfile",
						Aliases: []string{"o"},
						Value: "proxies.txt",
						DefaultText: "proxies.txt",
					},
					&cli.StringSliceFlag{
						Name: "types",
						Aliases: []string{"t"},
					},
					&cli.StringSliceFlag{
						Name: "countries",
						Aliases: []string{"c"},
					},
					&cli.StringSliceFlag{
						Name: "ports",
						Aliases: []string{"p"},
					},
					&cli.StringFlag{
						Name: "lvl",
						Aliases: []string{"l"},
					},
				},
				Action: func(c *cli.Context) error {
					// Run cmd using go run main.go grab"
					err := cmds.Grab(c, assetFS)
					return err
				},
			},
			{
				Name:    "serve",
				Aliases: []string{"g"},
				Usage:   "Serve web service api that serves proxies",
				Flags: []cli.Flag{
					&cli.IntFlag{
						Name: "port",
						Aliases: []string{"p"},
						Value: 8080,
						DefaultText: "8080",
					},
					&cli.BoolFlag{
						Name: "rest",
						Aliases: []string{"r"},
						Value: true,
						DefaultText: "true",
					},
				},
				Action: func(c *cli.Context) error {
					// Run cmd using go run main.go grab"
					err := cmds.Serve(c, assetFS);
					return err;
				},
			},
		},
		// Global flags information
		// https://github.com/urfave/cli/issues/325
		Flags: []cli.Flag{
			&cli.BoolFlag{
				Name: "verbose",
				Aliases: []string{"v"},
				Value: false,
				DefaultText: "false",
			},
		},
		Action: func(c *cli.Context) error {
			fmt.Println("ProxyBroker find");
			return nil;
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}

}
