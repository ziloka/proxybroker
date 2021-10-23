package services

import (
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"regexp"
	"time"
)


type sourceStruct struct {
	Url string `json:"url"`
	Type string `json:"type"`
}

func getProxies() []string {
	// https://www.golangprograms.com/golang-read-json-file-into-struct.html
	file, _ := ioutil.ReadFile("assets/sources.json")
	data := []sourceStruct{}
	json.Unmarshal([]byte(file), &data)
	var sources []string
	for _, source := range data {
		if source.Type == "http" {
			sources = append(sources, source.Url)
		}
	}
	return sources
}

func Collect(ch chan []string) {
	sources := getProxies()
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
		proxiesFromSource := re.FindAllString(content, -1)
		ch <- proxiesFromSource
		log.Printf("Found %v proxies from source %v\n", len(proxiesFromSource), url)
	}
	log.Printf("Debug there are %d proxies\n", len(ch))
	close(ch)
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