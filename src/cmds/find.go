package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
	"os"
)

func Find(c *cli.Context, assetFS embed.FS) (err error) {

	// Set default values for flags
	raw := c.Bool("raw")
	verbose := c.Bool("verbose")
	types := c.StringSlice("types")
	limit := c.Int("limit")
	countries := c.StringSlice("countries")
	ports := c.IntSlice("ports")

	bytes, _ := assetFS.ReadFile("assets/GeoLite2-Country.mmdb")

	db, _ := geoip2.FromBytes(bytes)
	defer db.Close()

	// Collect proxies
	quit := make(chan bool)
	proxies := make(chan []structs.Proxy, 99999)
	services.Collect(assetFS, db, quit, proxies, types, countries, ports, verbose)
	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}

	// Check Proxies
	// https://stackoverflow.com/questions/41906146/why-go-channels-limit-the-buffer-size
	// https://stackoverflow.com/a/41906488
	checkedProxies := make(chan structs.Proxy, 99999)
	waitForProxies:
		for {
			select {
				case proxiesArr := <- proxies:
					if verbose {
						fmt.Printf("Received %d proxies\n", len(proxiesArr))
					}
					for _, proxy := range proxiesArr {
						go services.Check(checkedProxies, publicIpAddr, proxy, verbose)
					}
				case <-quit:
					break waitForProxies
			}
		}

	index := 0
	for proxy := range checkedProxies {
		if index < limit {
			index++
			if raw {
				fmt.Println(proxy.Proxy)
			} else {
				fmt.Printf("<Proxy %v %v %+v>\n", proxy.Country, proxy.ConnDuration, proxy.Proxy)
			}
		} else {
			os.Exit(0)
		}
	}

	return err
}
