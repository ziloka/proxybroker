use lazy_static::lazy_static;
use regex::Regex;
use serde::Deserialize;
use tokio::sync::mpsc::Sender;

// https://serde.rs/field-attrs.html
#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
struct Source {
    #[serde(rename(deserialize = "url"))]
    url: String,
    #[serde(rename(deserialize = "type"))]
    protocol: String,
}

lazy_static! {
    static ref PROXY_REGEX: Regex = Regex::new(r"(\d+\.\d+\.\d+\.\d+):(\d+)").unwrap();
}

#[derive(Debug)]
pub struct Proxy {
    pub host: String,
    pub port: u16,
}

fn get_proxy_sources() -> Vec<Source> {
    // https://stackoverflow.com/questions/27140634/how-to-embed-resources-in-rust-executable
    // https://stackoverflow.com/questions/25505275/is-there-a-good-way-to-include-external-resource-data-into-rust-source-code
    serde_json::from_str(&String::from_utf8_lossy(include_bytes!(
        "../assets/sources.json"
    )))
    .unwrap()
}

async fn get_proxies_from_site(sender: Sender<Vec<Proxy>>, url: String) {
    let request = reqwest::get(&url);
    match request.await {
        Ok(response) => {
            match response.text().await {
                Ok(body) => {
                    let mut proxies: Vec<Proxy> = Vec::new();
                    for caps in PROXY_REGEX.captures_iter(&body) {
                        proxies.push(Proxy {
                            host: caps.get(1).unwrap().as_str().to_string(),
                            port: caps.get(2).unwrap().as_str().parse::<u16>().unwrap(),
                        })
                    }
                    match sender.try_send(proxies) {
                        Ok(_) => {}
                        Err(e) => println!("Could not send proxies from collector file: {}", e),
                    };
                }
                Err(e) => println!("Problem while getting request body: {}", e),
            };
        }
        Err(e) => println!("Problem while executing get request: {}", e),
    };
}

// https://www.reddit.com/r/rust/comments/dh99xn/help_multiple_http_requests_on_a_singlethread/
pub fn collect(sender: Sender<Vec<Proxy>>) {
    for proxy_source in get_proxy_sources()
        .into_iter()
        .filter(|s| s.protocol.eq("http"))
        .collect::<Vec<_>>()
    {
        tokio::task::spawn(get_proxies_from_site(sender.clone(), proxy_source.url));
    }
}
