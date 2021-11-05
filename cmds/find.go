package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
	"time"
)

func Find(c *cli.Context, assetFS embed.FS) (err error) {

	// Set default values for flags
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

	bytes, readFileError := assetFS.ReadFile("assets/GeoLite2-Country.mmdb")

	if readFileError != nil {
		return readFileError
	}

	db, dbErr := geoip2.FromBytes(bytes)
	if err != nil {
		return dbErr
	}
	defer db.Close()

	// Collect proxies
	proxies := make(chan []structs.Proxy)
	go services.Collect(assetFS, db, proxies, types, countries, ports, verbose)
	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}
	start := time.Now()
	// Check Proxies
	// https://stackoverflow.com/questions/41906146/why-go-channels-limit-the-buffer-size
	// https://stackoverflow.com/a/41906488
	checkedProxies := make(chan structs.Proxy, 500)
	for _, proxy := range <-proxies {
		// https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
		go services.Check(checkedProxies, publicIpAddr, proxy)
	}

	if verbose {
		fmt.Printf("Time took to check proxies: %v\n", time.Since(start))
	}

	index := 0
	for proxy := range checkedProxies {
		if index < limit {
			index++
			fmt.Printf("<Proxy %v %v %+v>\n", proxy.Country, proxy.ConnDuration, string(proxy.Proxy))
		} else {
			break
		}
	}

	return nil

}
