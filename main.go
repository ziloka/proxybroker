package main

import (
	"fmt"
	"log"
	"net"
	"os"
	"runtime"
	"strings"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
)

func main() {
	app := &cli.App{
		Name:  "ProxyBroker",
		Usage: "proxybroker find",
		Flags: []cli.Flag{
			&cli.StringFlag{
				Name:  "find",
				Value: "http",
				Usage: "proxy protocol",
			},
		},
		Action: func(c *cli.Context) error {

			runtime.GOMAXPROCS(4)

			proxies := services.Collect()
			publicIpAddr, err := services.GetpublicIpAddr()
			if err != nil {
				return err
			}
			checkedProxies := []string{}
			for _, proxy := range proxies {
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
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
