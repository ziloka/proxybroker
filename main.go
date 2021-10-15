package main

import (
	"fmt"
	"log"
	"net"
	"os"
	"strings"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
)

func main() {
	app := &cli.App{
		Name: "ProxyBroker",
		Usage: "proxybroker find",
		Flags: []cli.Flag {
			&cli.StringFlag{
				Name: "find",
				Value: "http",
				Usage: "proxy protocol",
			},
		},
		Action: func(c *cli.Context) error {
			proxies := services.Collect()
			publicIpAddr, err := services.GetpublicIpAddr()
			if err != nil {
				return err
			}
			checkedProxies := []string{}
			for _, proxy := range proxies {
				// https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
				isOnline := make(chan bool, 1)
				go services.Check(publicIpAddr, proxy, isOnline)
				result := <- isOnline
				if result {
					checkedProxies = append(checkedProxies, proxy)
					if(len(checkedProxies) >= 10){
						break;
					}
				}
			}

			db, dbErr := geoip2.Open("GeoLite2-Country.mmdb")
			if err != nil {
				return dbErr
			}
			defer db.Close()
			for _, proxy := range checkedProxies {
				host := strings.Split(proxy, ":")[0]
				ip := net.ParseIP(host)
				record, recordErr := db.Country(ip)
				if recordErr != nil {
					return recordErr
				}
				fmt.Printf("<Proxy %v %v>\n", record.Country.IsoCode, proxy)
			}
			return nil
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}