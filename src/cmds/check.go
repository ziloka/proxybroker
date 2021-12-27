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

	fileName := c.String("file")
	verbose := c.Bool("verbose")
	raw := c.Bool("raw")

	fmt.Printf("Using %s file for proxies\n", fileName);
	fileContents, err := os.ReadFile(fileName)
	if err != nil {
		panic(err)
	}

	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}

	proxies := []structs.Proxy{};
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
			fmt.Printf("<Proxy %v %v %+v>\n", proxy.Country, proxy.ConnDuration, proxy.Proxy)
		}
		proxies = append(proxies, proxy);
	}

	return nil
}