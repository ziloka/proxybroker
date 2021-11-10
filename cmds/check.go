package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/urfave/cli/v2"
	"os"
	"strings"
)

func Check(c *cli.Context, assetFS embed.FS) (err error) {

	file := c.String("file")
	verbose := c.Bool("verbose")
	raw := c.Bool("raw")

	dat, err := os.ReadFile(file)
	if err != nil {
		panic(err)
	}

	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}

	checkedProxies := make(chan structs.Proxy, 500)
	for _, proxy := range strings.Split(string(dat), "\n") {
		proxyStruct := structs.Proxy{
			Proxy: proxy,
		}
		// https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
		go services.Check(checkedProxies, publicIpAddr, proxyStruct, verbose)
	}

	for proxy := range checkedProxies {
		if raw {
			fmt.Println(proxy.Proxy)
		} else {
			fmt.Printf("<Proxy %v %v %+v>\n", proxy.Country, proxy.ConnDuration, proxy.Proxy)
		}
	}

	return nil
}
