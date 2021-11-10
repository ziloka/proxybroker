package cmds

import (
	"embed"
	"fmt"
	"os"

	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
)

func Find(c *cli.Context, assetFS embed.FS) (err error) {

	// Set default values for flags
	raw := c.Bool("raw")
	verbose := c.Bool("verbose")
	types := c.StringSlice("types")
	if len(types) == 0 {
		types = []string{"http", "https"}
	}
	timeout := c.Int("timeout")
	if timeout == 0 {
		timeout = 5000
	}
	limit := c.Int("limit")
	if limit == 0 {
		limit = 10
	}
	countries := c.StringSlice("countries")
	ports := c.StringSlice("ports")

	zipBytes, _ := assetFS.ReadFile("assets/GeoLite2-Country.zip")
	bytes := utils.ReadZIP(zipBytes)

	db, _ := geoip2.FromBytes(bytes)
	defer db.Close()

	// Collect proxies
	proxies := make(chan []structs.Proxy, 100000)
	services.Collect(assetFS, db, proxies, types, countries, ports, verbose)
	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}
	// Check Proxies
	// https://stackoverflow.com/questions/41906146/why-go-channels-limit-the-buffer-size
	// https://stackoverflow.com/a/41906488
	checkedProxies := make(chan structs.Proxy, 5000)
	for _, proxy := range <-proxies {
		// https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
		go services.Check(checkedProxies, publicIpAddr, proxy, verbose)
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

	return nil

}
