package cmds

import (
	"embed"
	"fmt"
	"os"
	"strings"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
	"github.com/Ziloka/ProxyBroker/services"
)

func Grab(c *cli.Context, assetFS embed.FS) (err error) {

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

	db, dbErr := geoip2.Open("assets/GeoLite2-Country.mmdb")
	if err != nil {
		return dbErr
	}
	defer db.Close()

	proxies := make(chan []string)
	go services.Collect(assetFS, db, proxies, types, countries, ports)

	displayedProxies := []string{}
	for _, proxy := range <-proxies {
		displayedProxies = append(displayedProxies, proxy)
		fmt.Println("[+] "+ proxy)
	}
	
	if true {
		data := []byte(strings.Join(displayedProxies, "\n"))
		f, fileCreateErr := os.Create("proxies.txt")
		if fileCreateErr != nil {
			panic(fileCreateErr)
		}
		fileWriteErr := os.WriteFile("proxies.txt", data, 0644)
		if fileWriteErr != nil {
			panic(fileWriteErr)
		}
		defer f.Close()

	}
	return nil
}