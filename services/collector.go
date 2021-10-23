package services

import (
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"regexp"
	"strings"
	"time"
	"github.com/oschwald/geoip2-golang"
	"github.com/Ziloka/ProxyBroker/utils"
)


type sourceStruct struct {
	Url string `json:"url"`
	Type string `json:"type"`
}

func getProxies(types []string) []string {
	// https://www.golangprograms.com/golang-read-json-file-into-struct.html
	file, _ := ioutil.ReadFile("assets/sources.json")
	data := []sourceStruct{}
	json.Unmarshal([]byte(file), &data)
	var sources []string
	for _, source := range data {
		if len(types) == 0 || utils.Contains(types, source.Type) {
			sources = append(sources, source.Url)
		}
	}
	return sources
}

func Collect(db *geoip2.Reader, ch chan []string, types []string, countries []string, ports []string) {
	sources := getProxies(types)
	fmt.Printf("Found %v sources\n", len(sources))
	httpClient := &http.Client{ Timeout: 10 * time.Second}
	for _, url := range sources {
		// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
		// https://stackoverflow.com/a/31129967
		res, httpErr := httpClient.Get(url)
		if httpErr != nil {
			continue;
		}
		defer res.Body.Close()
		b, _ := io.ReadAll(res.Body)
		content := string(b)
		re, _ := regexp.Compile(`\d+\.\d+\.\d+\.\d+:\d+`)
		proxies := re.FindAllString(content, -1)
		valid := []string{}
		// filter proxies
		for _, proxy := range proxies {
			host := strings.Split(proxy, ":")[0]
			port := strings.Split(proxy, ":")[1]
			ip := net.ParseIP(host)
			record, recordErr := db.Country(ip)
			if recordErr != nil {
				continue;
			}
			country := record.Country.IsoCode
			if (utils.Contains(ports, port) || len(ports) == 0) && (utils.Contains(countries, country) || len(countries) == 0) {
				valid = append(valid, proxy)
			}
		}
		ch <- valid
		log.Printf("Found %v proxies from source %v\n", len(proxies), url)
	}
	log.Printf("Debug there are %d proxies\n", len(ch))
	defer close(ch)
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