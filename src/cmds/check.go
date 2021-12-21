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

	inputFile := c.String("input")
	verbose := c.Bool("verbose")
	raw := c.Bool("raw")

	fmt.Printf("Using %s file for proxies\n", inputFile)
	fileContents, err := os.ReadFile(inputFile)
	if err != nil {
		panic(err)
	}

	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}

	fmt.Println(strings.Split(string(fileContents), "\n"))

	proxies := []structs.Proxy{}
	checkedProxies := make(chan structs.Proxy, 99999)
	for _, proxy := range strings.Split(string(fileContents), "\n") {
		proxyStruct := structs.Proxy{
			Proxy: proxy,
		}
		// https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
		go services.Check(checkedProxies, &proxies, publicIpAddr, proxyStruct, verbose)
	}

	for proxy := range checkedProxies {
		if raw {
			fmt.Println(proxy.Proxy)
		} else {
			fmt.Printf("<Proxy %s %s %s %+s>\n", proxy.Country, proxy.ConnDuration, proxy.Protocol, proxy.Proxy)
		}
	}

	return nil
}
