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
  host: String,
  port: u16
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

pub async fn collect() -> Vec<Proxy> {
  let sources = get_proxy_sources();
  let re = Regex::new(r"(\d+\.\d+\.\d+\.\d+):(\d+)").unwrap();
  let mut proxies: Vec<Proxy> = Vec::new();
  for proxy_source in sources {
    let request = reqwest::get(&proxy_source.url);
    match request.await {
      Ok(response) => {
        println!("{} {}", proxy_source.url, response.status());
        let future_text = response.text();
        let body = match future_text.await {
          Ok(r) => r,
          Err(e) => panic!("Problem while getting request body: {}", e)
        };
        for caps in re.captures_iter(&body) {
          proxies.push(Proxy { host: caps.get(1).unwrap().as_str().to_string(), port: caps.get(2).unwrap().as_str().parse::<u16>().unwrap() })
        }
      },
      Err(e) => println!("Problem while executing get request: {}", e)
    };
  }
  proxies
}