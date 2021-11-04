package main

import (
  "embed"
  "fmt"
  "github.com/Ziloka/ProxyBroker/cmds"
  "github.com/urfave/cli/v2"
  "log"
  "os"
  "runtime"
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
        Name:    "find",
        Aliases: []string{"f"},
        Usage:   "Find and check proxies",
        Flags: []cli.Flag{
          &cli.BoolFlag{Name: "verbose", Aliases: []string{"v"}},
          &cli.StringFlag{Name: "types", Aliases: []string{"t"}},
          &cli.StringFlag{Name: "timeout", Aliases: []string{"to"}},
          &cli.StringFlag{Name: "countries", Aliases: []string{"c"}},
          &cli.StringFlag{Name: "ports", Aliases: []string{"p"}},
          &cli.StringFlag{Name: "lvl", Aliases: []string{"l"}},
          &cli.StringFlag{Name: "limit"},
        },
        Action: func(c *cli.Context) error {
          // Run cmd using go run main.go find"
          err := cmds.Find(c, assetFS)
          return err
        },
      },
      {
        Name:    "grab",
        Aliases: []string{"g"},
        Usage:   "Grab proxies from sites",
        Flags: []cli.Flag{
          &cli.BoolFlag{Name: "verbose", Aliases: []string{"v"}},
          &cli.StringFlag{Name: "types", Aliases: []string{"t"}},
          &cli.StringFlag{Name: "timeout", Aliases: []string{"to"}},
          &cli.StringFlag{Name: "countries", Aliases: []string{"c"}},
          &cli.StringFlag{Name: "ports", Aliases: []string{"p"}},
          &cli.StringFlag{Name: "lvl", Aliases: []string{"l"}},
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
          &cli.BoolFlag{Name: "verbose", Aliases: []string{"v"}},
          &cli.StringFlag{Name: "port", Aliases: []string{"p"}},
        },
        Action: func(c *cli.Context) error {
          // Run cmd using go run main.go grab"
          err := cmds.Serve(c, assetFS)
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
