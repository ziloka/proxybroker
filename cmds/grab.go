package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
	"os"
	"strings"
)

func Grab(c *cli.Context, assetFS embed.FS) (err error) {

	// Set default values for flags
	outfile := c.String("outfile")
	verbose := c.Bool("verbose")
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

	zipBytes, readFileError := assetFS.ReadFile("assets/GeoLite2-Country.zip")
	bytes := utils.ReadZIP(zipBytes)

	if readFileError != nil {
		return readFileError
	}

	db, dbErr := geoip2.FromBytes(bytes)
	if err != nil {
		return dbErr
	}
	defer db.Close()

	proxies := make(chan []structs.Proxy)
	go services.Collect(assetFS, db, proxies, types, countries, ports, verbose)

	displayedProxies := []string{}
	for _, proxyStruct := range <-proxies {
		displayedProxies = append(displayedProxies, proxyStruct.Proxy)
	}

	if outfile == "" {
		for _, proxy := range displayedProxies {
			fmt.Println("[+] " + proxy)
		}
	} else {
		data := []byte(strings.Join(displayedProxies, "\n"))
		f, fileCreateErr := os.Create("proxies.txt")
		if fileCreateErr != nil {
			panic(fileCreateErr)
		}
		fileWriteErr := os.WriteFile(outfile, data, 0644)
		if fileWriteErr != nil {
			panic(fileWriteErr)
		}
		defer f.Close()
	}

	return nil
}
