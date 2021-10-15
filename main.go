package main

import (
	"fmt"
	"log"
	"net"
	"os"
	"runtime"
	"strings"
	"time"
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
				// fmt.Printf("Amount of proxies: %v\n", len(checkedProxies))
				// fmt.Printf("Proxies: %v\n", checkedProxies)
				if len(checkedProxies) >= 10 {
					break
				}
			}

				time.Sleep(1*time.Second)

			db, dbErr := geoip2.Open("GeoLite2-Country.mmdb")
			if err != nil {
				return dbErr
			}
			defer db.Close()

			// max :=100
			// count :=0
			// for (len(checkedProxies) <= 9 || count<max ){
			// 	fmt.Printf("Amount of proxies: %v\n", len(checkedProxies))
			// 	count = count +1
			// }
			

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
