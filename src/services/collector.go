package services

import (
	"embed"
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"regexp"
	"strconv"
	"strings"
	"time"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/oschwald/geoip2-golang"
)

type sourceStruct struct {
	Url  string `json:"url"`
	Type string `json:"type"`
}

func Collect(assetFS embed.FS, db *geoip2.Reader, quit chan bool, ch chan []structs.Proxy, types []string, countries []string, ports []int, isVerbose bool) {
	sources := getProxies(assetFS, types)
	if isVerbose {
		fmt.Printf("Found %v sources\n", len(sources))
	}

	getProxies := func(source sourceStruct, lastElement bool) {

		httpClient := &http.Client{
			Timeout: 5 * time.Second,
		}

		// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
		// https://stackoverflow.com/a/31129967
		res, httpErr := httpClient.Get(source.Url)
		if(lastElement){
			quit <- true
		}
		// fmt.Printf("%s\n", source.Url)
		if httpErr != nil {
			// check if website that was down was last website
			fmt.Printf("%s\n", httpErr)
			return
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
			portStr := strings.Split(proxy, ":")[1]
			port, _ := strconv.Atoi(portStr)
			ip := net.ParseIP(host)
			record, recordErr := db.Country(ip)
			if recordErr != nil {
				continue
			}
			country := record.Country.IsoCode
			if (utils.IntContains(ports, port) || len(ports) == 0) && (utils.StringContains(countries, country) || len(countries) == 0) {
				proxyStruct := structs.Proxy{
					Proxy:    proxy,
					Protocol: source.Type,
					Country:  country,
				}
				valid = append(valid, proxyStruct)
			}
		}
		if isVerbose {
			fmt.Printf("Found %v proxies from source %v\n", len(proxies), source.Url)
		}
		ch <- valid
	}

	// proxies that are easy to collect
	for i, source := range sources {
		go getProxies(source, i == len(sources)-1)
	}

	if isVerbose {
		fmt.Printf("Debug there are %d proxies\n", len(ch))
	}
}

func getProxies(assetFS embed.FS, types []string) []sourceStruct {
	// https://www.golangprograms.com/golang-read-json-file-into-struct.html
	file, _ := assetFS.ReadFile("assets/sources.json")
	sources := []sourceStruct{}
	json.Unmarshal([]byte(file), &sources)
	valid := []sourceStruct{}

	if(len(types) > 0) {
		// lowercase all types
		tempTypes := types
		types = []string{}
		for _, source := range tempTypes {
			types = append(types, strings.ToLower(source))
		}

		for _, source := range sources {
			if utils.StringContains(types, source.Type) {
				valid = append(valid, source)
			}
		}
	} else {
		valid = sources
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
