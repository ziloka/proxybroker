package cmds

import (
	"fmt"
	"net"
	"strings"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
	"github.com/Ziloka/ProxyBroker/services"
)

func Find(c *cli.Context) (err error) {

	// Collect proxies
	proxies := make(chan []string)
	go services.Collect(proxies)
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

	db, dbErr := geoip2.Open("assets/GeoLite2-Country.mmdb")
	if err != nil {
		return dbErr
	}
	defer db.Close()

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