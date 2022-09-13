use crate::services;
use clap::ArgMatches;
use crossbeam::channel::bounded;
use std::fs::File;
use std::io::prelude::*;

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
    let (unchecked_proxies_tx, unchecked_proxies_rx) =
        bounded::<Vec<crate::services::collector::Proxy>>(100);
    let (checked_proxies_tx, checked_proxies_rx) =
        bounded::<crate::services::checker::CheckProxyResponse>(100);
    services::collector::collect(unchecked_proxies_tx.clone()); // if not cloned throws Disconnected Error, otherwise throws Empty
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
                    if counter >= *limit {
                        std::process::exit(0);
                    }
                }
            }
            Err(_) => {} // Err(e) => println!("Checked Proxies queue Error: {e}")
        }
    }
}
