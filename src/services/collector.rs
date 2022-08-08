use std::future::Future;
use futures::stream::FuturesUnordered;
use serde::Deserialize;
use rust_embed::RustEmbed;
use regex::Regex;

#[derive(RustEmbed)]
#[folder = "src/assets/"]
struct Assets;

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
  let sources = Assets::get("sources.json");
  let content = match sources {
    Some(file) => file.data,
    None => panic!("sources.json not there")
  };
  serde_json::from_str(&String::from_utf8_lossy(&content.into_owned()))
    .expect("error while reading or parsing")
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