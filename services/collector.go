package services

import (
	"embed"
	"encoding/json"
	"io"
	"net"
	"net/http"
	"regexp"
	"strings"
	"time"

	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/oschwald/geoip2-golang"
	log "github.com/sirupsen/logrus"
)

type sourceStruct struct {
	Url  string `json:"url"`
	Type string `json:"type"`
}

func getProxies(assetFS embed.FS, types []string) []string {
	// https://www.golangprograms.com/golang-read-json-file-into-struct.html
	file, _ := assetFS.ReadFile("assets/sources.json")
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

func Collect(assetFS embed.FS, db *geoip2.Reader, ch chan []structs.Proxy, types []string, countries []string, ports []string) {
	sources := getProxies(assetFS, types)
	log.Debug("Found %v sources\n", len(sources))
	httpClient := &http.Client{
		Timeout: 16 * time.Second,
		Transport: &http.Transport{
			MaxIdleConns: 100,
			MaxConnsPerHost:  100,
			MaxIdleConnsPerHost: 100,
		},
	}
	for _, url := range sources {
		// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
		// https://stackoverflow.com/a/31129967
		res, httpErr := httpClient.Get(url)
		if httpErr != nil {
			// fmt.Println(httpErr)
			continue
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
					Proxy: proxy,
				}
				valid = append(valid, proxyStruct)
			}
		}
		ch <- valid
		log.Debugf("Found %v proxies from source %v\n", len(proxies), url)
	}
	log.Debugf("Debug there are %d proxies\n", len(ch))
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
