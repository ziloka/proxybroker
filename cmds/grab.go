package cmds

import (
	"fmt"
	"os"
	"strings"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/urfave/cli/v2"
)

func Grab(*cli.Context) (err error) {
	proxies := make(chan []string)
	go services.Collect(proxies)

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