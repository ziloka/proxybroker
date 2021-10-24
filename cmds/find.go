package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
	"net"
	"strings"
)

func Find(c *cli.Context, assetFS embed.FS) (err error) {

	// Set default values for flags
	types := c.StringSlice("types")
	if len(types) == 0 {
		types = []string{"http", "https"}
	}
	timeout := c.Int("timeout")
	if timeout == 0 {
		timeout = 5000
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
	proxies := make(chan []string)
	go services.Collect(assetFS, db, proxies, types, countries, ports)
	publicIpAddr, err := services.GetpublicIpAddr()
	if err != nil {
		return err
	}
	// Check Proxies
	checkedProxies := []string{}
	for _, proxy := range <-proxies {
		// https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
		go services.Check(&checkedProxies, publicIpAddr, proxy)
	}

	// Golang while loop implementation
	for 1 < 2 {
		if len(checkedProxies) >= 10 {
			break
		}
	}

	for _, proxy := range checkedProxies[:10] {
		host := strings.Split(proxy, ":")[0]
		ip := net.ParseIP(host)
		record, recordErr := db.Country(ip)
		if recordErr != nil {
			return recordErr
		}
		country := record.Country.IsoCode
		if country == "" {
			country = "Unknown"
		}
		fmt.Printf("<Proxy %v %v>\n", country, proxy)
	}
	return nil

}
