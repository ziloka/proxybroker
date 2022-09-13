use crossbeam::channel::Sender;
use regex::Regex;
use serde::Deserialize;

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
    // puffin::profile_function!();
    let re = Regex::new(r"(\d+\.\d+\.\d+\.\d+):(\d+)").unwrap();
    let mut proxies: Vec<Proxy> = Vec::new();
    match reqwest::Client::builder().build() {
        Ok(client) => {
            let request = client.get(&url).send();
            match request.await {
                Ok(response) => {
                    let start = std::time::SystemTime::now();
                    match response.text().await {
                        Ok(body) => {
                            println!(
                                "took {}ms for {}",
                                start.elapsed().expect("Duration").as_millis(),
                                url
                            );
                            for caps in re.captures_iter(&body) {
                                proxies.push(Proxy {
                                    host: caps.get(1).unwrap().as_str().to_string(),
                                    port: caps.get(2).unwrap().as_str().parse::<u16>().unwrap(),
                                })
                            }
                            match sender.try_send(proxies) {
                                Ok(_) => {}
                                Err(e) => println!("Could not send proxies from collector file: {}", e)
                            };
                        }
                        Err(e) => println!("Problem while getting request body: {}", e),
                    };
                }
                Err(e) => println!("Problem while executing get request: {}", e),
            };
        }
        Err(e) => println!("{}", e),
    }
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
