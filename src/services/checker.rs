use std::future::Future;
use futures::stream::FuturesUnordered;
use serde::Deserialize;
use crate::services::collector::Proxy;

#[derive(Deserialize)]
struct HttpBinResponse {
    origin: String,
}

pub struct CheckProxyResponse {
  pub alive: bool,
  pub host: String,
  pub port: u16
}

/**
 * checks if it is possible to use proxy
 */
async fn send_proxy_request(proxy: Proxy) -> Result<CheckProxyResponse, reqwest::Error> {
  let response = reqwest::Client::builder().proxy(reqwest::Proxy::http(format!("http://{}:{}", proxy.host, proxy.port))?).build()?.get("http://httpbin.org/ip").send().await?.json::<HttpBinResponse>().await?;
  return Ok(CheckProxyResponse{
    alive: response.origin.eq(&proxy.host),
    host: proxy.host,
    port: proxy.port,
  });
  // if response.origin.eq(proxy.host) {
  //   return Ok(CheckProxyResponse{
  //     host: proxy.host,
  //     port: proxy.port,
  //   });
  // } else {
  //   return Ok(CheckProxyResponse{
  //     host: proxy.host,
  //     port: proxy.port,
  //   });
  // }
}

pub fn check(proxies: Vec<Proxy>) -> FuturesUnordered<impl Future<Output = Result<CheckProxyResponse, reqwest::Error>>> {
  let list_of_futures = FuturesUnordered::new();
  for proxy in proxies {
    list_of_futures.push(send_proxy_request(proxy));
  }
  list_of_futures
}