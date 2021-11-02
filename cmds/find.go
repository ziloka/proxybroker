package cmds

import (
	"embed"
	"fmt"
  "time"
	"net"
	"strings"
	"github.com/Ziloka/ProxyBroker/services"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/oschwald/geoip2-golang"
	"github.com/urfave/cli/v2"
)

func Find(c *cli.Context, assetFS embed.FS) (err error) {

  // Set default values for flags
  types := c.StringSlice("types")
  if len(types) == 0 {
    types = []string{"http", "https"}
  }
  timeout := c.Int("timeout")
  if timeout == 0 {
    timeout = 5000
  }
  limit := c.Int("limit")
  if limit == 0 {
    limit = 10
  }
  countries := c.StringSlice("countries")
  ports := c.StringSlice("ports")

  bytes, readFileError := assetFS.ReadFile("assets/GeoLite2-Country.mmdb")

  if readFileError != nil {
    return readFileError
  }

  db, dbErr := geoip2.FromBytes(bytes)
  if err != nil {
    return dbErr
  }
  defer db.Close()

  // Collect proxies
  proxies := make(chan []structs.Proxy)
  go services.Collect(assetFS, db, proxies, types, countries, ports)
  publicIpAddr, err := services.GetpublicIpAddr()
  if err != nil {
    return err
  }
  start := time.Now()
  // Check Proxies
  // https://stackoverflow.com/questions/41906146/why-go-channels-limit-the-buffer-size
  // https://stackoverflow.com/a/41906488
  checkedProxies := make(chan structs.Proxy, 500)
  for _, proxy := range <-proxies {
    // https://reshefsharvit.medium.com/common-pitfalls-and-cases-when-using-goroutines-15107237d4f5
    go services.Check(checkedProxies, publicIpAddr, proxy.Proxy)
  }
  fmt.Println(time.Since(start))

  index := 0
  for proxy := range checkedProxies {
    if index < limit {
      host := strings.Split(proxy.Proxy, ":")[0]
      ip := net.ParseIP(host)
      record, recordErr := db.Country(ip)
      if recordErr != nil {
        return recordErr
      }
      index++
      country := record.Country.IsoCode
      if country == "" {
        country = "Unknown"
      }
      fmt.Printf("<Proxy %v %.2fs %+v>\n", country, proxy.AvgRespTime.Seconds(), string(proxy.Proxy))
    } else {
      break 
    }
  }

  // index := 0
  // for proxy := range checkedProxies {
  //   if index < limit {
  //     host := strings.Split(proxy.Proxy, ":")[0]
  //     ip := net.ParseIP(host)
  //     record, recordErr := db.Country(ip)
  //     if recordErr != nil {
  //       return recordErr
  //     }
  //     index++
  //     country := record.Country.IsoCode
  //     if country == "" {
  //       country = "Unknown"
  //     }
  //     fmt.Printf("<Proxy %v %.2fs %+v>\n", country, proxy.AvgRespTime.Seconds(), string(proxy.Proxy))
  //   } else {
  //     break
  //   }
  // }

  return nil

}
