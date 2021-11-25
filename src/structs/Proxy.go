package structs

import (
	"time"
)

type Proxy struct {
	IsOnline     bool
	Proxy        string        `json:"proxy"`
	Protocol     string        `json:"protocol"`
	AvgRespTime  time.Duration `json:"avgRespTime"`
	ConnDuration time.Duration `json:"connDuration"`
	Country      string        `json:"country"`
	IsoCode      string        `json:"isoCode"`
	ReqDuration  time.Duration `json:"reqDuration"`
}
