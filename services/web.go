package services

import (
  "embed"
  "encoding/json"
  "fmt"
  "github.com/Ziloka/ProxyBroker/structs"
  "github.com/Ziloka/ProxyBroker/utils"
  "github.com/oschwald/geoip2-golang"
  "log"
  "net/http"
  "time"
)

func handler(w http.ResponseWriter, r *http.Request) {
  fmt.Fprintf(w, "Hi there, I love %s!", r.URL.Path[1:])
}

func StartWebService(assetFS embed.FS, port string, verbose bool) {

  checkedProxies := make(chan structs.Proxy, 500)
  bytes, readFileError := assetFS.ReadFile("assets/GeoLite2-Country.mmdb")

  if readFileError != nil {
    return
  }

  db, dbErr := geoip2.FromBytes(bytes)
  if dbErr != nil {
    return
  }

  ticker := utils.NewTicker(5, time.Minute)
  quit := make(chan string)
  go func() {
    for {
      select {
      case <-ticker.C:

        // Collect proxies
        proxies := make(chan []structs.Proxy, 500)
        go Collect(assetFS, db, proxies, nil, nil, nil, verbose)
        publicIpAddr, err := GetpublicIpAddr()
        if err != nil {
          return
        }
        // Check Proxies
        for _, proxy := range <-proxies {
          // https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
          go Check(checkedProxies, publicIpAddr, proxy, verbose)
        }

      case <-quit:
        ticker.Stop()
        return
      }
    }
  }()

  http.HandleFunc("/", handler)
  http.HandleFunc("/api", func(w http.ResponseWriter, r *http.Request) {

    jsonResponse, jsonErr := json.Marshal(checkedProxies)
    if jsonErr != nil {
      return
    }

    fmt.Fprint(w, string(jsonResponse))

  })

  // https://stackoverflow.com/a/11124241
  log.Fatal(http.ListenAndServe(fmt.Sprintf(":%v", port), nil))

}
