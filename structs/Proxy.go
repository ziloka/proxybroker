package structs

import (
	"time"
)

type Proxy struct {
	IsOnline     bool
	Protocol     string        `json:"protocol"`
	Proxy        string        `json:"proxy"`
	AvgRespTime  time.Duration `json:"avgRespTime"`
	ConnDuration time.Duration `json:"connDuration"`
	ReqDuration  time.Duration `json:"reqDuration"`
	Country      string        `json:"country"`
	IsoCode      string        `json:"isoCode"`
}
