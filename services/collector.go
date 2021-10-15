package services

import (
	"encoding/json"
	"fmt"
	"time"
	"io/ioutil"
	"io"
	"strings"
	"net/http"
)


type sourceStruct struct {
	Url string `json:"url"`
	Type string `json:"type"`
}

func getProxies() []string {
	// https://www.golangprograms.com/golang-read-json-file-into-struct.html
	file, _ := ioutil.ReadFile("sources.json")
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

func Collect() []string {
	sources := getProxies()
	fmt.Printf("Found %v sources\n", len(sources))
	proxies := make([]string, 0)
	httpClient := &http.Client{ Timeout: 10 * time.Second}
	for _, url := range sources {
		// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
		// https://stackoverflow.com/a/31129967
		res, httpErr := httpClient.Get(url)
		if httpErr != nil {
			
		}
		defer res.Body.Close()
		b, _ := io.ReadAll(res.Body)
		content := string(b)
		proxiesFromSource := strings.Split(content, "\n")
		fmt.Printf("Debug there are %d proxies\n", len(proxiesFromSource))
		for _, proxy := range proxiesFromSource {
			proxies = append(proxies, proxy)
		}
	}
	return proxies
}