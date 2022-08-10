use std::future::Future;
use futures::stream::FuturesUnordered;
use serde::Deserialize;
use regex::Regex;

// https://serde.rs/field-attrs.html
#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
struct Source {
  #[serde(rename(deserialize = "url"))]
  url: String,
  #[serde(rename(deserialize = "type"))]
  protocol: String,
}

pub struct Proxy {
  pub host: String,
  pub port: u16
}

fn get_proxy_sources () -> Vec<Source> {
  // https://stackoverflow.com/questions/27140634/how-to-embed-resources-in-rust-executable
  // https://stackoverflow.com/questions/25505275/is-there-a-good-way-to-include-external-resource-data-into-rust-source-code
  serde_json::from_str(&String::from_utf8_lossy(include_bytes!("../assets/sources.json"))).unwrap()
}

async fn get_proxies_from_site(url: String) -> Vec<Proxy> {
  let re = Regex::new(r"(\d+\.\d+\.\d+\.\d+):(\d+)").unwrap();
  let mut proxies: Vec<Proxy> = Vec::new();
  let request = reqwest::get(&url);
    match request.await {
      Ok(response) => {
        match response.text().await {
          Ok(body) => {
            for caps in re.captures_iter(&body) {
              proxies.push(Proxy { host: caps.get(1).unwrap().as_str().to_string(), port: caps.get(2).unwrap().as_str().parse::<u16>().unwrap() })
            }
          },
          Err(e) => println!("Problem while getting request body: {}", e)
        };
      },
      Err(e) => println!("Problem while executing get request: {}", e)
    };
    proxies
}

// https://www.reddit.com/r/rust/comments/dh99xn/help_multiple_http_requests_on_a_singlethread/
pub fn collect() -> FuturesUnordered<impl Future<Output = Vec<Proxy>>> {
  let list_of_futures = FuturesUnordered::new();
  for proxy_source in get_proxy_sources() {
    list_of_futures.push(get_proxies_from_site(proxy_source.url));
  }
  list_of_futures
}