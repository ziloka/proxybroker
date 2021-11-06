package structs

import (
  "fmt"
  "github.com/miekg/dns"
)

// func ReverseDNSLookup(target string) []dns.RR {
  
// }

// https://stackoverflow.com/questions/30043248/why-golang-lookup-function-cant-provide-a-server-parameter/31627459#31627459
// https://stackoverflow.com/a/31627459
func lookupDNS(target string) []dns.RR {
  server := "8.8.8.8"
  c := dns.Client{}
  m := dns.Msg{}
  m.SetQuestion(target+".", dns.TypeA)
  r, _, err := c.Exchange(&m, server+":53")
  if err != nil {
    r.Answer = []dns.RR{}
  }
  if len(r.Answer) == 0 {
    r.Answer = []dns.RR{}
  }
  fmt.Println(target)
  return r.Answer
}