use crate::services;
use clap::ArgMatches;
use std::fs::File;
use std::io::prelude::*;
use tokio::sync::mpsc::channel;

pub fn find(sub_matches: &ArgMatches) {
    let mut file: Option<Result<File, std::io::Error>> = None;
    if sub_matches.is_present("file") {
        file = Some(File::create(
            sub_matches
                .get_one::<String>("file")
                .expect("Specify filename to write proxies to"),
        ));
    }

    let limit = sub_matches.get_one::<u64>("limit").unwrap();
    let (unchecked_proxies_tx, mut unchecked_proxies_rx) =
        channel::<Vec<crate::services::collector::Proxy>>(100);
    let (checked_proxies_tx, mut checked_proxies_rx) =
        channel::<crate::services::checker::CheckProxyResponse>(100);
    services::collector::collect(unchecked_proxies_tx); // if not cloned throws Disconnected Error, otherwise throws Empty
    let mut counter: u64 = 0;

    loop {
        match unchecked_proxies_rx.try_recv() {
            Ok(proxies) => services::checker::check(checked_proxies_tx.clone(), proxies),
            Err(_) => {} // Err(e) => println!("Unchecked Proxies queue Error: {e}")
        }
        match checked_proxies_rx.try_recv() {
            Ok(proxy) => {
                if proxy.alive {
                    counter += 1;
                    println!("{}:{}", proxy.host, proxy.port);
                    if let Some(ref value) = file {
                        match &value {
                            Ok(file) => {
                                if let Err(err) = <&std::fs::File>::clone(&file)
                                    .write(format!("{}:{}\n", proxy.host, proxy.port).as_bytes())
                                {
                                    println!("Could not write to file: {}", err);
                                };
                            }
                            Err(err) => println!("No file to write to: {}", err),
                        }
                    }
                    if counter >= *limit {
                        std::process::exit(0);
                    }
                }
            }
            Err(_) => {} // Err(e) => println!("Checked Proxies queue Error: {e}")
        }
    }
}
