package cmds

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
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
	countries := c.StringSlice("countries")
	ports := c.IntSlice("ports")

	bytes, readFileError := assetFS.ReadFile("assets/GeoLite2-Country.mmdb")

	if readFileError != nil {
		return readFileError
	}

	db, dbErr := geoip2.FromBytes(bytes)
	if err != nil {
		return dbErr
	}
	defer db.Close()

	quit := make(chan bool)
	proxies := make(chan []structs.Proxy)
	services.Collect(assetFS, db, quit, proxies, types, countries, ports, verbose)

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
		f, fileCreateErr := os.Create(outfile)
		if fileCreateErr != nil {
			panic(fileCreateErr)
		}
		fileWriteErr := os.WriteFile(outfile, data, 0644)
		if fileWriteErr != nil {
			panic(fileWriteErr)
		}
		defer f.Close()
		fmt.Printf("Wrote %d proxies to %s\n", len(displayedProxies), outfile);
	}

	return nil
}
