package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/urfave/cli/v2"
)

func Serve(c *cli.Context, assetFS embed.FS) (err error) {

	verbose := c.Bool("verbose")
	port := c.String("port")
	isRestService := c.Bool("rest")
	if port == "" {
		port = "8080"
	}
	fmt.Printf("Running web service at http://127.0.0.1:%v\n", port)
	services.StartService(assetFS, port, verbose, isRestService)

	return nil
}
