package structs

import "time"

type Proxy struct {
  Protocol     string        `json:"protocol"`
  Proxy        string        `json:"proxy"`
  AvgRespTime  time.Duration `json:"avgRespTime"`
  ConnDuration time.Duration `json:"connDuration"`
  ReqDuration  time.Duration `json:"reqDuration"`
  Country      string        `json:"country"`
  IsoCode      string        `json:"isoCode"`
  HostName     []string        `json:"hostName"`
}
