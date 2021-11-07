package services

import (
	"embed"
	"encoding/json"
	"fmt"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/mxschmitt/playwright-go"
	"github.com/oschwald/geoip2-golang"
	"io"
	"log"
	"net"
	"net/http"
	"regexp"
	"strconv"
	"strings"
	"time"
)

type sourceStruct struct {
	Url  string `json:"url"`
	Type string `json:"type"`
}

func Collect(assetFS embed.FS, db *geoip2.Reader, ch chan []structs.Proxy, types []string, countries []string, ports []string, isVerbose bool) {
	sources := getProxies(assetFS, types)
	if isVerbose {
		fmt.Printf("Found %v sources\n", len(sources))
	}
	httpClient := &http.Client{
		Timeout: 16 * time.Second,
		Transport: &http.Transport{
			MaxIdleConns:        100,
			MaxConnsPerHost:     100,
			MaxIdleConnsPerHost: 100,
		},
	}

	// proxies that are easy to collect
	for _, source := range sources {
		// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
		// https://stackoverflow.com/a/31129967
		res, httpErr := httpClient.Get(source.Url)
		if httpErr != nil {
			panic(httpErr)
		}
		defer res.Body.Close()
		b, _ := io.ReadAll(res.Body)
		content := string(b)
		re, _ := regexp.Compile(`\d+\.\d+\.\d+\.\d+:\d+`)
		proxies := re.FindAllString(content, -1)
		valid := []structs.Proxy{}
		// filter proxies
		for _, proxy := range proxies {
			host := strings.Split(proxy, ":")[0]
			port := strings.Split(proxy, ":")[1]
			ip := net.ParseIP(host)
			record, recordErr := db.Country(ip)
			if recordErr != nil {
				continue
			}
			country := record.Country.IsoCode
			if (utils.Contains(ports, port) || len(ports) == 0) && (utils.Contains(countries, country) || len(countries) == 0) {
				proxyStruct := structs.Proxy{
					Proxy:    proxy,
					Protocol: source.Type,
					Country:  country,
				}
				valid = append(valid, proxyStruct)
			}
		}
		ch <- valid
		if isVerbose {
			fmt.Printf("Found %v proxies from source %v\n", len(proxies), source.Url)
		}
	}
	if isVerbose {
		fmt.Printf("Debug there are %d proxies\n", len(ch))
	}

	ch <- getProxiesFromNNTime(db)
	defer close(ch)
}

func getProxies(assetFS embed.FS, types []string) []sourceStruct {
	// https://www.golangprograms.com/golang-read-json-file-into-struct.html
	file, _ := assetFS.ReadFile("assets/sources.json")
	sources := []sourceStruct{}
	json.Unmarshal([]byte(file), &sources)
	valid := []sourceStruct{}

	// lowercase all types
	tempTypes := types
	types = []string{}
	for _, source := range tempTypes {
		types = append(types, strings.ToLower(source))
	}

	for _, source := range sources {
		if utils.Contains(types, source.Type) {
			valid = append(valid, source)
		}
	}
	return valid
}

func GetpublicIpAddr() (string, error) {
	res, err := http.Get("http://httpbin.org/ip?json")
	if err != nil {
		return "", err
	}
	defer res.Body.Close()
	obj := &HttpResponse{}
	json.NewDecoder(res.Body).Decode(obj)
	return obj.Origin, nil
}

func getProxiesFromNNTime(db *geoip2.Reader) []structs.Proxy {
	proxies := []structs.Proxy{}

	getProxiesOnPage := func(ch chan []structs.Proxy, pageNum string) ([]structs.Proxy, int) {
		numOfPages := 0
		var proxies []structs.Proxy
		pw, err := playwright.Run(&playwright.RunOptions{
			SkipInstallBrowsers: true,
			Browsers:            []string{"chromium"},
		})
		if err != nil {
			log.Fatalf("could not start playwright: %v", err)
		}
		browser, err := pw.Chromium.Launch()
		if err != nil {
			log.Fatalf("could not launch browser: %v", err)
		}
		page, err := browser.NewPage()
		if err != nil {
			log.Fatalf("could not create page: %v", err)
		}
		if _, err = page.Goto("http://nntime.com/proxy-list-" + pageNum + ".htm"); err != nil {
			log.Fatalf("could not goto: %v", err)
		}
		// https://stackoverflow.com/a/5418836
		pagesNumElement, err := page.QuerySelector("div#navigation > a:nth-last-child(3)")
		if err != nil {
			log.Fatalf("could not query selector: %v", err)
		}
		lastPageNum, err := pagesNumElement.InnerText()
		if err != nil {
			log.Fatalf("could not get inner text: %v", err)
		}

		lastPageNumInt, err := strconv.Atoi(lastPageNum)
		if err != nil {
			log.Fatalf("could not convert to int: %v", err)
		}
		numOfPages = lastPageNumInt
		entries, err := page.QuerySelectorAll("table#proxylist > tbody > tr")
		if err != nil {
			log.Fatalf("could not get entries: %v", err)
		}
		for _, entry := range entries {
			element, err := entry.QuerySelector("td:nth-child(2)")
			if err != nil {
				log.Fatalf("could not get title element: %v", err)
			}
			proxy, err := element.InnerText()
			if err != nil {
				log.Fatalf("could not get text content: %v", err)
			}
			proxies = append(proxies, structs.Proxy{
				Proxy: proxy,
			})

		}
		if err = browser.Close(); err != nil {
			log.Fatalf("could not close browser: %v", err)
		}
		if err = pw.Stop(); err != nil {
			log.Fatalf("could not stop Playwright: %v", err)
		}
		ch <- proxies
		return proxies, numOfPages
	}

	ch := make(chan []structs.Proxy, 200)
	_, numOfPages := getProxiesOnPage(ch, "01")
	for i := 2; i <= numOfPages; i++ {
		// https://stackoverflow.com/a/51546906
		go getProxiesOnPage(ch, fmt.Sprintf("%02d", i))
	}

i := 1
getProxies:
	for {
		select {
		case proxiesOnPage := <-ch:
			proxies = append(proxies, proxiesOnPage...)
			i++
			if i > numOfPages {
				break getProxies
			}
		}
	}
	return proxies
}
