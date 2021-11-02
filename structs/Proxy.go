package structs

import "time"

type Proxy struct {
	Proxy string `json:"proxy"`
	AvgRespTime time.Duration `json:"avgRespTime"`
	Country string `json:"country"`
}